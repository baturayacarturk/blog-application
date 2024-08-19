package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.responses.post.GetPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.services.post.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


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

    @GetMapping(path = "/get-posts")
    public ResponseEntity<GetPost> getAllSimplifiedPosts(
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "tag", required = false) String tag

    ) {
        GetPost response = postService.getPostDto(postId, title, text, userId, tag);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UpdatedPostResponse> updatePost(@RequestBody UpdatePostRequest postDetails) {
        UpdatedPostResponse updatedPost = postService.updatePost(postDetails);
        return ResponseEntity.ok(updatedPost);
    }


}
