package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetPost;

public interface PostService {
    CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest);
    GetPost getPosts();

}
