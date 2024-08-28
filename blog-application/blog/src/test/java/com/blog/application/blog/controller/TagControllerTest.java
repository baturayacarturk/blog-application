package com.blog.application.blog.controller;

import com.blog.application.blog.controllers.TagController;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.CoreMatchers.is;

@ActiveProfiles("test")

@WebMvcTest(controllers = TagController.class)
@ComponentScan(basePackages = {"com.blog.application.blog.jwt"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TagController.class, TagController.class})
public class TagControllerTest {

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenRepository tokenRepository;

    @Test
    @WithMockUser(username = "john_doe")
    void testAddTagToPost() throws Exception {
        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(1L);

        when(tagService.addTagToPost(1L, tagDto)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagDto)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(response.getName())))
                .andExpect(jsonPath("$.postId", is(response.getPostId().intValue())));
    }
    @Test
    void testAddTagToPostWhenForbidden() throws Exception {
        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(1L);

        when(tagService.addTagToPost(1L, tagDto)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagDto)));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testRemoveTagFromPost() throws Exception {
        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        when(tagService.removeTag(1L, 1L)).thenReturn(tagDto);

        ResultActions resultActions = mockMvc.perform(delete("/api/tags/1")
                .param("tagId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tagDto.getName())));
    }
    @Test
    void testRemoveTagFromPostWhenForbidden() throws Exception {
        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");

        when(tagService.removeTag(1L, 1L)).thenReturn(tagDto);

        ResultActions resultActions = mockMvc.perform(delete("/api/tags/1")
                .param("tagId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }
}
