package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.tag.TagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;

import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;

import com.blog.application.blog.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, UserService userService) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
    }

    @Override
    public CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest) {
        User extractedUser = extractUserNameFromSecurityContext();
        Optional<User> user = userService.findByUsername(extractedUser.getUsername());
        if (user.isEmpty()) {
            throw new BusinessException("User not found");
        }
        if (!Objects.equals(user.get().getId(), createPostRequest.getUserId())) {
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        Post post = createdPostRequestToPostEntity(createPostRequest, user.get());
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
        User extractedUser = extractUserNameFromSecurityContext();
        if (!extractedUser.getUsername().equals(post.getUser().getUsername())) {
            throw new BusinessException("User not found");
        }
        post.getTags().add(tag);
        postRepository.save(post);
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
        User extractedUser = extractUserNameFromSecurityContext();
        if (!postList.get(0).getUser().getId().equals(extractedUser.getId())) {
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }

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
    public DeletedPostResponse deletePostById(Long postId) {
        Post post = postRepository.getPostEntity(postId);
        if (post == null) {
            throw new BusinessException("Post not found");
        }
        User currentUser = extractUserNameFromSecurityContext();
        Optional<User> user = userService.findByUsername(currentUser.getUsername());
        if (!user.isPresent()) {
            throw new BusinessException("User not found");
        }
        boolean matchedPost = user.get().getPosts().stream().anyMatch(p -> p.getId().equals(postId));
        if (!matchedPost) {
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        user.get().getPosts().remove(post);
        post.getTags().clear();
        postRepository.delete(post);
        DeletedPostResponse response = new DeletedPostResponse();
        response.setResponse(String.format("Post with id: %d is successfully deleted.", postId));
        return response;
    }


    @Override
    @Transactional
    public UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest) {
        User extractedUser = extractUserNameFromSecurityContext();
        Post post = postRepository.getPostEntity(updatePostRequest.getId());
        if (!post.getUser().getId().equals(extractedUser.getId())) {
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


    private Post createdPostRequestToPostEntity(CreatePostRequest createPostRequest, User user) {
        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setTitle(createPostRequest.getTitle());
        post.setUser(user);
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

    private static User extractUserNameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        return (User)principalUser;
    }
}
