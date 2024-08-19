package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;

import java.util.List;

public interface PostService {
    CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest);
    GetPost getPostDto(Long postId, String title, String text, Long userId, String tag);
    UpdatedPostResponse updatePost(UpdatePostRequest updatePostRequest);
    List<Post> getPostEntities(Long postId, String title, String text, Long userId, String tag);
    Post addTagToPost(Long postId, Tag tag);
    Post removeTagFromPost(Post post);

}
