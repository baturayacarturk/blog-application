package com.blog.application.blog.integration;


import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void testCreatePost() throws Exception {
        TagDto tagDto = new TagDto("Sample Tag");
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Title");
        request.setText("Text");
        request.setTags(List.of(tagDto));

        ResultActions resultActions = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Title")))
                .andExpect(jsonPath("$.text", is("Text")))
                .andExpect(jsonPath("$.tags.length()", is(1)))
                .andExpect(jsonPath("$.tags[0].name", is(tagDto.getName())));

        // Retrieve the created post from the repository
        List<Post> posts = postRepository.getAllPostEntitites();
        assertThat(posts).hasSize(1);

        Post createdPost = posts.get(0);
        assertThat(createdPost.getTitle()).isEqualTo("Title");
        assertThat(createdPost.getText()).isEqualTo("Text");
        assertThat(createdPost.getTags()).hasSize(1);

        Tag createdTag = createdPost.getTags().iterator().next();
        assertThat(createdTag.getName()).isEqualTo(tagDto.getName());
    }

    @Test
    void testGetAllSimplifiedPosts() throws Exception {
        TagDto tagDto1 = new TagDto( "Sample Tag");
        TagDto tagDto2 = new TagDto( "Other Tag");
        postService.createBlogPost(new CreatePostRequest("Sample Title", "Sample Text", null, List.of(tagDto1)));
        postService.createBlogPost(new CreatePostRequest("Another Title", "Another Text", null, List.of(tagDto2)));

        ResultActions resultActions = mockMvc.perform(get("/api/posts/get-simplified-posts")
                .accept(MediaType.APPLICATION_JSON));

        // Retrieve posts from the repository
        List<Post> postsFromRepo = postRepository.findAll();

        assertThat(postsFromRepo).hasSize(2);
        Post post1 = postsFromRepo.stream()
                .filter(post -> "Sample Title".equals(post.getTitle()))
                .findFirst()
                .orElseThrow();
        Post post2 = postsFromRepo.stream()
                .filter(post -> "Another Title".equals(post.getTitle()))
                .findFirst()
                .orElseThrow();

        assertThat(post1.getTitle()).isEqualTo("Sample Title");
        assertThat(post1.getText()).isEqualTo("Sample Text");

        assertThat(post2.getTitle()).isEqualTo("Another Title");
        assertThat(post2.getText()).isEqualTo("Another Text");

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()", is(postsFromRepo.size())))
                .andExpect(jsonPath("$.posts[0].title", is(post1.getTitle())))
                .andExpect(jsonPath("$.posts[0].text", is(post1.getText())))
                .andExpect(jsonPath("$.posts[1].title", is(post2.getTitle())))
                .andExpect(jsonPath("$.posts[1].text", is(post2.getText())));
    }


    @Test
    void testGetAllPostsWithTagId() throws Exception {
        //TODO fix when user comes in to picture
        TagDto tagDto = new TagDto("Sample Tag");
        CreatePostRequest createPostRequest = new CreatePostRequest("Sample Post", "Sample Text", null, List.of(tagDto));
        CreatedSimpleBlogPost createdSimpleBlogPost = postService.createBlogPost(createPostRequest);
        Long tagId = createdSimpleBlogPost.getTags().get(0).getId();


        ResultActions resultActions = mockMvc.perform(get("/api/posts/get-by-tag/" +tagId)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Sample Post")))
                .andExpect(jsonPath("$[0].text", is("Sample Text")))
                .andExpect(jsonPath("$[0].tags.length()", is(1)))
                .andExpect(jsonPath("$[0].tags[0].name", is(tagDto.getName())));

        List<Post> posts = postRepository.getPostEntityByTagId(tagId);

        assertThat(posts).hasSize(1);
        Post retrievedPost = posts.get(0);
        assertThat(retrievedPost.getTitle()).isEqualTo("Sample Post");
        assertThat(retrievedPost.getText()).isEqualTo("Sample Text");
        assertThat(retrievedPost.getTags()).hasSize(1);

        Tag retrievedTag = retrievedPost.getTags().iterator().next();
        assertThat(retrievedTag.getId()).isEqualTo(tagId);
        assertThat(retrievedTag.getName()).isEqualTo(tagDto.getName());

    }

    @Test
    void testUpdatePost() throws Exception {

        CreatePostRequest createRequest = new CreatePostRequest("Initial Title", "Initial Text", null, Collections.emptyList());
        CreatedSimpleBlogPost createdPostResponse = postService.createBlogPost(createRequest);

        Post createdPost = postRepository.getPostEntity(createdPostResponse.getPostId());

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setId(createdPost.getId());
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated Text");


        ResultActions resultActions = mockMvc.perform(put("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(createdPost.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.text", is("Updated Text")));

        Post updatedPost = postRepository.getPostEntity(createdPost.getId());
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedPost.getText()).isEqualTo("Updated Text");
    }

}
