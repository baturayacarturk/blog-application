package com.blog.application.blog.integration;

import com.blog.application.blog.dtos.common.TagDto;
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



import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void testAddTagToPost() throws Exception {
        Post post = new Post();
        post.setTitle("Sample Post");
        post.setText("Sample Text");
        postRepository.save(post);

        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        ResultActions resultActions = mockMvc.perform(post("/api/tags/{postId}", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagDto)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(tagDto.getName())))
                .andExpect(jsonPath("$.postId", is(post.getId().intValue())));

        Post updatedPost = postRepository.getPostEntity(post.getId());
        assertThat(updatedPost.getTags()).hasSize(1);
        Tag addedTag = updatedPost.getTags().iterator().next();
        assertThat(addedTag.getName()).isEqualTo(tagDto.getName());
    }

    @Test
    void testRemoveTagFromPost() throws Exception {
        Post post = new Post();
        post.setTitle("Another Post");
        post.setText("Another Text");
        postRepository.save(post);

        Tag tag = new Tag();
        tag.setName("Tag to Remove");
        tagRepository.save(tag);

        post.getTags().add(tag);
        postRepository.save(post);

        ResultActions resultActions = mockMvc.perform(delete("/api/tags/{postId}", post.getId())
                .param("tagId", tag.getId().toString()));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag.getName())));

        Post updatedPost = postRepository.getPostEntity(post.getId());
        assertThat(updatedPost.getTags()).isEmpty();
    }
}
