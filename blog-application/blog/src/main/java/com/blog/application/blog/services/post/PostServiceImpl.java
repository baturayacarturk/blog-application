package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.helpers.params.GetPostsQueryBuilder;
import com.blog.application.blog.helpers.params.PostSearchParams;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final GetPostsQueryBuilder getPostsQueryBuilder;
    private final EntityManager entityManager;

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, GetPostsQueryBuilder getPostsQueryBuilder, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.getPostsQueryBuilder = getPostsQueryBuilder;
        this.entityManager = entityManager;
    }

    @Override
    public CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest) {
        Post post = createdPostRequestToPostEntity(createPostRequest);
        //TODO inject TagService instead repository some business logic can be required.
        //TODO change tag name uniqueness
        tagRepository.saveAll(post.getTags());

        Post savedPost = postRepository.save(post);
        CreatedSimpleBlogPost createdPostDto = new CreatedSimpleBlogPost();
        createdPostDto.setTitle(savedPost.getTitle());
        createdPostDto.setText(savedPost.getText());
        //TODO validate if user null and exists.
        createdPostDto.setUserId(createdPostDto.getUserId());
        createdPostDto.setTags(createPostRequest.getTags());

        return createdPostDto;

    }

    @Override
    public GetPost getPostDto(Long postId, String title, String text, Long userId, String tag) {
        List<Post> posts = getPostEntities(postId, title, text, userId, tag);
        GetPost result = new GetPost();
        posts.forEach(post -> {
            PostDto postDto = new PostDto();
            postDto.setPostId(post.getId());
            postDto.setText(post.getText());
            postDto.setTitle(post.getTitle());
            Set<TagDto> tagDtos = post.getTags().stream()
                    .map(resultTag -> {
                        TagDto tagDto = new TagDto();
                        tagDto.setName(resultTag.getName());
                        return tagDto;
                    })
                    .collect(Collectors.toSet());
            postDto.setTags(tagDtos);
            result.getPosts().add(postDto);
        });

        return result;
    }

    @Override
    public List<Post> getPostEntities(Long postId, String title, String text, Long userId, String tag) {
        PostSearchParams postSearchParams = PostSearchParams.builder()
                .id(postId)
                .title(title)
                .text(text)
                .userId(userId)
                .tagNames(tag)
                .build();
        String queryString = getPostsQueryBuilder.buildQuery(postSearchParams);
        Map<String, Object> parameters = getPostsQueryBuilder.getParameters(postSearchParams);
        TypedQuery<Post> query = entityManager.createQuery(queryString, Post.class);
        parameters.forEach(query::setParameter);
        List<Post> posts = query.getResultList();
        return posts;
    }

    @Override
    public Post addTagToPost(Long postId, Tag tag) {
        List<Post> post = getPostEntities(postId,null,null,null,null);
        //TODO validate
        Post resultPost = post.get(0);
        resultPost.getTags().add(tag);
        postRepository.save(resultPost);
        return resultPost;
    }

    @Override
    public Post removeTagFromPost(Post post) {
        Post response = new Post();
        if(post!=null){
            response = postRepository.save(post);
        }
        return response;
    }

    @Override
    @Transactional
    public UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest) {
        List<Post> postOptional = getPostEntities(updatePostRequest.getId(), null, null, null, null);
        if (postOptional == null) {
            //TODO add validation and handle errors
        }
        Post post = postOptional.get(0);
        post.setTitle(updatePostRequest.getTitle() != null ? updatePostRequest.getTitle() : post.getTitle());
        post.setText(updatePostRequest.getText() != null ? updatePostRequest.getText() : post.getText());
        var updatedPostEntity = postRepository.save(post);
        UpdatedPostResponse updatedPostResponse = new UpdatedPostResponse();
        updatedPostResponse.setPostId(updatedPostEntity.getId());
        updatedPostResponse.setText(updatedPostEntity.getText());
        updatedPostResponse.setTitle(updatedPostEntity.getTitle());
        return updatedPostResponse;
    }


    private Post createdPostRequestToPostEntity(CreatePostRequest createPostRequest) {
        //TODO switch to auto mapping
        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setTitle(createPostRequest.getTitle());
        //TODO check whether user is exists
        post.setId(createPostRequest.getUserId());

        Set<Tag> tags = new HashSet<>();
        createPostRequest.getTags().forEach(tagDto -> {
            Tag tag = new Tag();
            tag.setName(tagDto.getName());
            tags.add(tag);
        });
        post.setTags(tags);

        return post;
    }

}
