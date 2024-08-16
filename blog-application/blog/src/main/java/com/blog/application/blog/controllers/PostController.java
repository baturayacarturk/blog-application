package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.responses.post.GetPost;
import com.blog.application.blog.services.post.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;



@RestController
@RequestMapping(path = "/api/posts",produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class PostController {

    private PostService postService;

    @PostMapping
    public ResponseEntity<CreatedSimpleBlogPost> createPost(@RequestBody CreatePostRequest createPostRequest){
        CreatedSimpleBlogPost createdPost = postService.createBlogPost(createPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }
    @GetMapping(path = "/get-posts")
    public ResponseEntity<GetPost> getAllSimplifiedPosts(){
        GetPost response = postService.getPosts();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }



}
