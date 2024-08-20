package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.post.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {
    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private Tag tag;
    private Set<Tag> tags;


    @BeforeEach void setup(){
        tag = new Tag();
        tag.setName("@Sofia");
        tag.setId(1L);
        tags = new HashSet<>();
        tags.add(tag);

        post = new Post(1L,"My blog","This is my first post",null,tags);
    }
    @Test
    public void givenCreatePostRequest_whenCreateBlogPost_thenReturnCreatedSimpleBlogPost(){
        CreatePostRequest createPostRequest = new CreatePostRequest();
        TagDto tagDto = new TagDto();
        tagDto.setName("@Sofia");

        createPostRequest.setTitle("abc");
        createPostRequest.setText("sss");
        createPostRequest.setTags(List.of(tagDto));
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(tagRepository.saveAll(anySet())).thenReturn(tags);

        CreatedSimpleBlogPost createdPostDto = postService.createBlogPost(createPostRequest);

        assertEquals(post.getTitle(), createdPostDto.getTitle());
        assertEquals(post.getText(), createdPostDto.getText());
        assertEquals(post.getTags().size(), createdPostDto.getTags().size());
    }
}
