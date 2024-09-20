package com.blog.application.blog.integration;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private TagService tagService;

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();

    }

    @Test
    void testAddTagToPost() throws Exception {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Sample Post");
        createPostRequest.setText("Sample Text");
        createPostRequest.setUserId(10L);

        CreatedSimpleBlogPost createdSimpleBlogPost = postService.createBlogPost(createPostRequest);

        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        ResultActions resultActions = mockMvc.perform(post("/api/tags/" + createdSimpleBlogPost.getPostId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagDto)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(tagDto.getName())))
                .andExpect(jsonPath("$.postId", is(createdSimpleBlogPost.getPostId().intValue())));

        Post updatedPost = postRepository.getPostEntity(createdSimpleBlogPost.getPostId());
        assertThat(updatedPost.getTags()).hasSize(1);
        Tag addedTag = updatedPost.getTags().iterator().next();
        assertThat(addedTag.getName()).isEqualTo(tagDto.getName());
    }

    @Test
    void testRemoveTagFromPost() throws Exception {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        TagDto tagDto = new TagDto();
        tagDto.setName("Tag to Remove");
        createPostRequest.setTitle("Sample Post");
        createPostRequest.setText("Sample Text");
        createPostRequest.setUserId(10L);
        createPostRequest.setTags(List.of(tagDto));


        CreatedSimpleBlogPost createdSimpleBlogPost = postService.createBlogPost(createPostRequest);
        var tagToRemove = createdSimpleBlogPost.getTags().get(0);

        ResultActions resultActions = mockMvc.perform(delete("/api/tags/" + createdSimpleBlogPost.getPostId().toString() + "?tagId=" + tagToRemove.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tagToRemove.getName())));

        Post updatedPost = postRepository.getPostEntity(createdSimpleBlogPost.getPostId());
        assertThat(updatedPost.getTags()).isEmpty();
    }

}
