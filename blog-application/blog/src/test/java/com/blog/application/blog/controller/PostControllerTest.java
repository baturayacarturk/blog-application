package com.blog.application.blog.controller;

import com.blog.application.blog.controllers.PostController;
import com.blog.application.blog.controllers.UserController;
import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.dtos.responses.tag.TagResponse;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class)
@ComponentScan(basePackages = {"com.blog.application.blog.jwt"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {PostController.class, PostService.class})

public class PostControllerTest {
    @MockBean
    private PostService postService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private PostController postController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Test
    @WithMockUser(username = "john_doe")
    void testCreatePost() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        userRepository.save(user);

        TagDto tagDto = new TagDto("Sample Tag");
        CreatePostRequest request = new CreatePostRequest();
        request.setText("Text");
        request.setTitle("Title");
        request.setTags(List.of(tagDto));
        request.setUserId(1L);

        TagResponse tagResponse = new TagResponse();
        tagResponse.setName("Sample Tag");

        CreatedSimpleBlogPost response = new CreatedSimpleBlogPost();
        response.setText("Text");
        response.setTitle("Title");
        response.setPostId(1L);
        response.setTags(List.of(tagResponse));

        Mockito.when(postService.createBlogPost(Mockito.any(CreatePostRequest.class))).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.text", is(response.getText())))
                .andExpect(jsonPath("$.tags.length()", is(1)))
                .andExpect(jsonPath("$.tags[0].name", is(tagDto.getName())));
    }

    @Test
    void testCreatePostWhenForbidden() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        userRepository.save(user);

        TagDto tagDto = new TagDto("Sample Tag");
        CreatePostRequest request = new CreatePostRequest();
        request.setText("Text");
        request.setTitle("Title");
        request.setTags(List.of(tagDto));
        request.setUserId(1L);

        TagResponse tagResponse = new TagResponse();
        tagResponse.setName("Sample Tag");

        CreatedSimpleBlogPost response = new CreatedSimpleBlogPost();
        response.setText("Text");
        response.setTitle("Title");
        response.setPostId(1L);
        response.setTags(List.of(tagResponse));

        Mockito.when(postService.createBlogPost(Mockito.any(CreatePostRequest.class))).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isForbidden());
    }


    @Test
    void testGetAllSimplifiedPosts() throws Exception {
        SimplifiedPost simplifiedPost = new SimplifiedPost();
        simplifiedPost.setTitle("Sample Title");
        simplifiedPost.setText("Sample Text");
        SimplifiedPost simplifiedPost2 = new SimplifiedPost();
        simplifiedPost2.setTitle("A");
        simplifiedPost2.setText("B");

        GetAllSimplifiedPost response = new GetAllSimplifiedPost();
        response.setPosts(List.of(simplifiedPost, simplifiedPost2));
        when(postService.getAllSimplifiedPosts()).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/posts/get-simplified-posts")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()", is(2)));
    }

    @Test
    void testGetAllPostsWithTagId() throws Exception {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setName("Sample Tag");
        GetAllByTagId getAllByTagId = new GetAllByTagId();
        getAllByTagId.setTitle("Sample Post Title");
        getAllByTagId.setText("Sample Post Text");
        getAllByTagId.setPostId(1L);
        getAllByTagId.setTags(List.of(tagResponse));
        List<GetAllByTagId> response = Collections.singletonList(getAllByTagId);
        when(postService.getPostsByTagId(1L)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/posts/get-by-tag/1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId", is(getAllByTagId.getPostId().intValue())))
                .andExpect(jsonPath("$[0].title", is(getAllByTagId.getTitle())))
                .andExpect(jsonPath("$[0].text", is(getAllByTagId.getText())))
                .andExpect(jsonPath("$[0].tags.length()", is(1)))
                .andExpect(jsonPath("$[0].tags[0].name", is(tagResponse.getName())));
    }


    @Test
    @WithMockUser(username = "john_doe")
    void testUpdatePost() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(1L);
        request.setTitle("Updated Title");
        request.setText("Updated Text");

        UpdatedPostResponse response = new UpdatedPostResponse();
        response.setPostId(1L);
        response.setTitle("Updated Title");
        response.setText("Updated Text");
        when(postService.updatePost(request)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(response.getPostId().intValue())))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.text", is(response.getText())));
    }

    @Test
    void testUpdatePostWhenForbidden() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(1L);
        request.setTitle("Updated Title");
        request.setText("Updated Text");

        UpdatedPostResponse response = new UpdatedPostResponse();
        response.setPostId(1L);
        response.setTitle("Updated Title");
        response.setText("Updated Text");
        when(postService.updatePost(request)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testDeletePost() throws Exception {
        DeletedPostResponse response = new DeletedPostResponse();
        response.setResponse("Post deleted successfully");

        when(postService.deletePostById(anyLong())).thenReturn(response);

        mockMvc.perform(delete("/api/posts/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testDeletePostWhenForbidden() throws Exception {
        DeletedPostResponse response = new DeletedPostResponse();
        response.setResponse("Post deleted successfully");

        when(postService.deletePostById(anyLong())).thenReturn(response);

        mockMvc.perform(delete("/api/posts/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
