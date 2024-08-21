package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetAllByTagId;
import com.blog.application.blog.dtos.responses.post.GetAllSimplifiedPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.dtos.responses.tag.TagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;

import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;

import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest) {
        Post post = createdPostRequestToPostEntity(createPostRequest);
        //TODO inject TagService instead repository some business logic can be required.
        tagRepository.saveAll(post.getTags());

        Post savedPost = postRepository.save(post);
        CreatedSimpleBlogPost createdPostDto = new CreatedSimpleBlogPost();
        createdPostDto.setTitle(savedPost.getTitle());
        createdPostDto.setText(savedPost.getText());
        createdPostDto.setPostId(savedPost.getId());
        createdPostDto.setTags(convertToTagResponseList(savedPost.getTags()));

        return createdPostDto;

    }

    @Override
    public GetAllSimplifiedPost getAllSimplifiedPosts() {
        List<SimplifiedPostProjection> simplifiedPostList = postRepository.getAllSimplifiedBlogPost();
        List<SimplifiedPost> simplifiedPosts = simplifiedPostList.stream()
                .map(projection -> new SimplifiedPost(projection.getTitle(), projection.getText()))
                .collect(Collectors.toList());

        return new GetAllSimplifiedPost(simplifiedPosts);
    }



    @Override
    public Post addTagToPost(Long postId, Tag tag) {
        Post post = postRepository.getPostEntity(postId);
        post.getTags().add(tag);
        postRepository.save(post);
        return post;
    }

    @Override
    public Post removeTagFromPost(Post post) {
        Post response = new Post();
        //TODO validate appropriately
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
    public List<Post> getAllPostEntities() {
        return getAllPostEntities();
    }


    @Override
    @Transactional
    public UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest) {
        Post post = postRepository.getPostEntity(updatePostRequest.getId());

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

}
