package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.responses.post.GetAllByTagId;
import com.blog.application.blog.dtos.responses.post.GetAllSimplifiedPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.dtos.responses.post.DeletedPostResponse;
import com.blog.application.blog.services.post.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * REST controller for managing blog posts.
 * <p>
 * This controller provides endpoints to create, retrieve, update, and delete blog posts.
 * </p>
 */
@RestController
@RequestMapping(path = "/api/posts", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Api(value = "Post Management", tags = "Posts")
public class PostController {

    private final PostService postService;

    /**
     * Creates a new blog post.
     *
     * @param createPostRequest the request object containing the details of the post to be created
     * @return a {@link ResponseEntity} containing the created post details
     */
    @ApiOperation(value = "Create a new blog post", notes = "Creates a new blog post and returns the created post details.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Post created successfully", response = CreatedSimpleBlogPost.class),
            @ApiResponse(code = 400, message = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<CreatedSimpleBlogPost> createPost(
            @ApiParam(value = "Details of the post to be created", required = true)
            @RequestBody CreatePostRequest createPostRequest) {
        CreatedSimpleBlogPost createdPost = postService.createBlogPost(createPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * Retrieves all simplified blog posts.
     *
     * @return a {@link ResponseEntity} containing a list of simplified blog posts
     */
    @ApiOperation(value = "Get all simplified blog posts", notes = "Retrieves a list of simplified blog posts.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Posts retrieved successfully", response = GetAllSimplifiedPost.class)
    })
    @GetMapping(path = "/get-simplified-posts")
    public ResponseEntity<GetAllSimplifiedPost> getAllSimplifiedPosts() {
        GetAllSimplifiedPost response = postService.getAllSimplifiedPosts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves posts associated with a given tag ID.
     *
     * @param tagId the ID of the tag
     * @return a {@link ResponseEntity} containing a list of posts associated with the tag ID
     */
    @ApiOperation(value = "Get posts by tag ID", notes = "Retrieves all posts associated with a given tag ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Posts retrieved successfully", response = GetAllByTagId.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Tag not found")
    })
    @GetMapping(path = "/get-by-tag/{tagId}")
    public ResponseEntity<List<GetAllByTagId>> getAllPostsWithTagId(
            @ApiParam(value = "ID of the tag", required = true)
            @PathVariable Long tagId) {
        List<GetAllByTagId> response = postService.getPostsByTagId(tagId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Updates an existing blog post.
     *
     * @param postDetails the request object containing updated details of the post
     * @return a {@link ResponseEntity} containing the updated post details
     */
    @ApiOperation(value = "Update a blog post", notes = "Updates an existing blog post and returns the updated details.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Post updated successfully", response = UpdatedPostResponse.class),
            @ApiResponse(code = 400, message = "Invalid input")
    })
    @PutMapping
    public ResponseEntity<UpdatedPostResponse> updatePost(
            @ApiParam(value = "Updated details of the post", required = true)
            @RequestBody UpdatePostRequest postDetails) {
        UpdatedPostResponse updatedPost = postService.updatePost(postDetails);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Deletes a blog post by its ID.
     *
     * @param postId the ID of the post to be deleted
     * @return a {@link ResponseEntity} containing the response of the deletion operation
     */
    @ApiOperation(value = "Delete a blog post", notes = "Deletes a blog post by its ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Post deleted successfully", response = DeletedPostResponse.class),
            @ApiResponse(code = 404, message = "Post not found")
    })
    @DeleteMapping(path = "/delete/{postId}")
    public ResponseEntity<DeletedPostResponse> deletePost(
            @ApiParam(value = "ID of the post to be deleted", required = true)
            @PathVariable Long postId) {
        DeletedPostResponse response = postService.deletePostById(postId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
