package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.services.post.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;


@RestController
@RequestMapping(path = "/api/posts", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class PostController {

    private PostService postService;

    @PostMapping
    public ResponseEntity<CreatedSimpleBlogPost> createPost(@RequestBody CreatePostRequest createPostRequest) {
        CreatedSimpleBlogPost createdPost = postService.createBlogPost(createPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping(path = "/get-simplified-posts")
    public ResponseEntity<GetAllSimplifiedPost> getAllSimplifiedPosts() {
        GetAllSimplifiedPost response = postService.getAllSimplifiedPosts();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping(path = "/get-by-tag/{tagId}")
    public ResponseEntity<List<GetAllByTagId>> getAllPostsWithTagId(@PathVariable Long tagId) {
        List<GetAllByTagId> response = postService.getPostsByTagId(tagId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UpdatedPostResponse> updatePost(@RequestBody UpdatePostRequest postDetails) {
        UpdatedPostResponse updatedPost = postService.updatePost(postDetails);
        return ResponseEntity.ok(updatedPost);
    }
    @DeleteMapping(path = "/delete/{postId}")
    public ResponseEntity<DeletedPostResponse> deletePost(@PathVariable Long postId){
        DeletedPostResponse response = postService.deletePostById(postId);
        return new ResponseEntity<>(response,HttpStatus.OK);


    }


}
