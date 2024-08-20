package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetAllSimplifiedPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;

import java.util.List;


public interface PostService {
    CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest);
    GetAllSimplifiedPost getAllSimplifiedPosts();
    UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest);
    Post addTagToPost(Long postId, Tag tag);
    Post removeTagFromPost(Post post);
    Post getPostEntity(Long postId);
    List<PostDto> getPostsByTagId(Long tagId);

}
