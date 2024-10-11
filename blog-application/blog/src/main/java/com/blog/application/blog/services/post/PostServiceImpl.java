package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.ElasticPostEvent;
import com.blog.application.blog.dtos.common.ElasticTagDto;
import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.client.UserClientDto;
import com.blog.application.blog.dtos.responses.elastic.SearchByKeywordResponse;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.dtos.responses.tag.TagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.entities.elastic.ElasticPost;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.producer.ElasticEventsProducer;
import com.blog.application.blog.producer.TagProducer;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.repositories.elastic.PostElasticRepository;
import com.blog.application.blog.services.client.UserFeignClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserFeignClient userFeignClient;
    private final PostElasticRepository elasticRepository;
    private final ElasticEventsProducer elasticEventsProducer;
    private final TagProducer tagProducer;
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, UserFeignClient userFeignClient, PostElasticRepository elasticRepository, ElasticEventsProducer elasticEventsProducer, TagProducer tagProducer) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.userFeignClient = userFeignClient;
        this.elasticRepository = elasticRepository;
        this.elasticEventsProducer = elasticEventsProducer;
        this.tagProducer = tagProducer;
    }

    @Override
    public CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest) {
        UserClientDto user = userFeignClient.getUserDetails().getBody();

        if (!Objects.equals(user.getId(), createPostRequest.getUserId())) {
            logger.error("Unauthorized access attempt by user: {} to create post for userId: {}", user.getUsername(), createPostRequest.getUserId());
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        Post post = createdPostRequestToPostEntity(createPostRequest, user);
        tagRepository.saveAll(post.getTags());

        Post savedPost = postRepository.save(post);

        ElasticPostEvent elasticPostEvent = new ElasticPostEvent(savedPost.getId(), savedPost.getTitle(), savedPost.getText(), savedPost.getUserId(), convertToElasticTagDtoList(savedPost.getTags()));
        elasticEventsProducer.sendElasticEvent(elasticPostEvent);

        CreatedSimpleBlogPost createdPostDto = new CreatedSimpleBlogPost();
        createdPostDto.setTitle(savedPost.getTitle());
        createdPostDto.setText(savedPost.getText());
        createdPostDto.setPostId(savedPost.getId());
        createdPostDto.setTags(convertToTagResponseList(savedPost.getTags()));

        return createdPostDto;

    }

    @Override
    public GetAllSimplifiedPost getAllSimplifiedPosts(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<SimplifiedPostProjection> page = postRepository.getAllSimplifiedBlogPost(pageable);

        List<SimplifiedPost> simplifiedPosts = page.getContent().stream()
                .map(projection -> new SimplifiedPost(projection.getTitle(), projection.getText()))
                .collect(Collectors.toList());
        return new GetAllSimplifiedPost(
                simplifiedPosts,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    @Override
    public Post addTagToPost(Long postId, Tag tag) {
        Post post = postRepository.getPostEntity(postId);
        UserClientDto user = userFeignClient.getUserDetails().getBody();
        if (!user.getId().equals(post.getUserId())) {
            logger.error("Unauthorized attempt to add tag to post by user: {}", user.getUsername());
            throw new BusinessException("User not found");
        }
        post.getTags().add(tag);
        tagProducer.sendMessage(post);
        return post;
    }

    @Override
    public Post removeTagFromPost(Post post) {
        Post response = new Post();
        if (post != null) {
            response = postRepository.save(post);
        }
        return response;
    }

    @Override
    public Post getPostEntity(Long id) {
        return postRepository.getPostEntity(id);
    }

    @Override
    public List<GetAllByTagId> getPostsByTagId(Long tagId) {
        List<Post> postList = postRepository.getPostEntityByTagId(tagId);
        return postList.stream()
                .map(this::convertToGetAllByTagId)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public DeletedPostResponse deletePostById(Long postId) {
        Post post = postRepository.getPostEntity(postId);
        if (post == null) {
            logger.error("Post not found with id: {}", postId);
            throw new BusinessException("Post not found");
        }
        UserClientDto user = userFeignClient.getUserDetails().getBody();
        boolean matchedPost = Objects.equals(post.getUserId(), user.getId());
        if (!matchedPost) {
            logger.error("Unauthorized attempt to delete post with id: {} by user: {}", postId, user.getUsername());
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        post.getTags().clear();
        postRepository.delete(post);
        DeletedPostResponse response = new DeletedPostResponse();
        response.setResponse(String.format("Post with id: %d is successfully deleted.", postId));
        return response;
    }

    @Override
    public List<SearchByKeywordResponse> searchByKeyword(String keyword) {
        List<ElasticPost> elasticPosts = elasticRepository.searchByKeyword(keyword);
        return elasticPosts.stream()
                .map(this::convertToSearchByKeywordResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest) {
        UserClientDto user = userFeignClient.getUserDetails().getBody();
        Post post = postRepository.getPostEntity(updatePostRequest.getId());
        if (!Objects.equals(post.getUserId(), user.getId())) {
            logger.error("Unauthorized attempt to update post with id: {} by user: {}", updatePostRequest.getId(), user.getUsername());
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }

        post.setTitle(updatePostRequest.getTitle() != null ? updatePostRequest.getTitle() : post.getTitle());
        post.setText(updatePostRequest.getText() != null ? updatePostRequest.getText() : post.getText());
        var updatedPostEntity = postRepository.save(post);

        UpdatedPostResponse updatedPostResponse = new UpdatedPostResponse();
        updatedPostResponse.setPostId(updatedPostEntity.getId());
        updatedPostResponse.setText(updatedPostEntity.getText());
        updatedPostResponse.setTitle(updatedPostEntity.getTitle());
        return updatedPostResponse;
    }


    private Post createdPostRequestToPostEntity(CreatePostRequest createPostRequest, UserClientDto user) {
        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setTitle(createPostRequest.getTitle());
        post.setUserId(user.getId());
        Set<Tag> tags = new HashSet<>();
        createPostRequest.getTags().forEach(tagDto -> {
            Tag tag = new Tag();
            tag.setName(tagDto.getName());
            tags.add(tag);
        });
        post.setTags(tags);

        return post;
    }

    private GetAllByTagId convertToGetAllByTagId(Post post) {
        GetAllByTagId getAllByTagId = new GetAllByTagId();
        getAllByTagId.setPostId(post.getId());
        getAllByTagId.setTitle(post.getTitle());
        getAllByTagId.setText(post.getText());
        getAllByTagId.setTags(convertToTagResponseList(post.getTags()));
        return getAllByTagId;
    }

    private List<TagResponse> convertToTagResponseList(Set<Tag> tags) {
        return tags.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    private TagResponse convertToTagResponse(Tag tag) {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setName(tag.getName());
        tagResponse.setId(tag.getId());
        return tagResponse;
    }

    private List<ElasticTagDto> convertToElasticTagDtoList(Set<Tag> tags) {
        return tags.stream()
                .map(tag -> new ElasticTagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    private SearchByKeywordResponse convertToSearchByKeywordResponse(ElasticPost elasticPost) {
        SearchByKeywordResponse searchByKeywordResponse = new SearchByKeywordResponse();
        searchByKeywordResponse.setId(elasticPost.getId());
        searchByKeywordResponse.setUserId(elasticPost.getUserId());
        searchByKeywordResponse.setTitle(elasticPost.getTitle());
        searchByKeywordResponse.setText(elasticPost.getText());
        searchByKeywordResponse.setTags(elasticPost.getTags());
        return searchByKeywordResponse;
    }
}