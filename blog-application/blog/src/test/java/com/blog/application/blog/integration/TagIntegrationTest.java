package com.blog.application.blog.integration;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.entities.Token;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private String jwtToken;
    private User savedUser;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setDisplayName("testUser");
        savedUser = userRepository.save(testUser);
        jwtToken = generateToken("testUser");
        Token token = new Token();
        token.setToken(jwtToken);
        token.setUser(testUser);
        tokenRepository.save(token);
    }

    @Test
    void testAddTagToPost() throws Exception {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Sample Post");
        createPostRequest.setText("Sample Text");
        createPostRequest.setUserId(savedUser.getId());
        setUpSecurityContext(savedUser);

        CreatedSimpleBlogPost createdSimpleBlogPost = postService.createBlogPost(createPostRequest);


        TagDto tagDto = new TagDto();
        tagDto.setName("Sample Tag");
        ResultActions resultActions = performRequestWithJwt("/api/tags/" + createdSimpleBlogPost.getPostId().toString(), "POST", tagDto);

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
        createPostRequest.setUserId(savedUser.getId());
        createPostRequest.setTags(List.of(tagDto));

        setUpSecurityContext(savedUser);

        CreatedSimpleBlogPost createdSimpleBlogPost = postService.createBlogPost(createPostRequest);
        var tagToRemove = createdSimpleBlogPost.getTags().get(0);
        ResultActions resultActions = performRequestWithJwt("/api/tags/" + createdSimpleBlogPost.getPostId().toString() + "?tagId=" + tagToRemove.getId().toString(), "DELETE", null);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tagToRemove.getName())));

        Post updatedPost = postRepository.getPostEntity(createdSimpleBlogPost.getPostId());
        assertThat(updatedPost.getTags()).isEmpty();
    }

    private static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "47A52F686696CABA4A9824E6177DFFFF5161ASDFDS1D2DS")
                .compact();
    }

    private ResultActions performRequestWithJwt(String url, String method, Object content) throws Exception {
        ResultActions resultActions;
        switch (method.toUpperCase()) {
            case "POST":
                resultActions = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(content))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
                break;
            case "PUT":
                resultActions = mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(content))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
                break;
            case "DELETE":
                resultActions = mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
                break;
            case "GET":
                resultActions = mockMvc.perform(get(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
        return resultActions;
    }

    private void setUpSecurityContext(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
