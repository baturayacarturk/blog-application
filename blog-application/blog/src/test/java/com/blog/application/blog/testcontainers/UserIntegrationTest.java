package com.blog.application.blog.testcontainers;

import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.entities.Token;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;


    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testUser");
        registerRequest.setPassword("password");

        ResultActions resultActions = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        Optional<User> userFromDb = userRepository.findByUsername("testUser");
        assertThat(userFromDb).isPresent();
        User user = userFromDb.get();
        assertThat(user.getUsername()).isEqualTo("testUser");
        List<Token> tokenFromDb = tokenRepository.findAllValidTokensByUser(user.getId());
        Token token = tokenFromDb.get(0);
        assertThat(token.getUser().getUsername().equals("testUser"));
        assertFalse(token.isExpired());
        assertFalse(token.isRevoked());
        assertThat(tokenFromDb).isNotNull();

    }

    @Test
    void testAuthenticate() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testUser");
        registerRequest.setPassword("password");
        userService.register(registerRequest);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testUser");
        authenticationRequest.setPassword("password");

        ResultActions resultActions = mockMvc.perform(post("/api/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authenticationRequest)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        Optional<User> userFromDb = userRepository.findByUsername("testUser");
        assertThat(userFromDb).isPresent();
        User user = userFromDb.get();
        assertThat(user.getUsername()).isEqualTo("testUser");
        List<Token> tokenFromDb = tokenRepository.findAllValidTokensByUser(user.getId());
        Token token = tokenFromDb.get(0);
        assertFalse(token.isRevoked());
        assertThat(token.getUser().getUsername().equals("testUser"));
        assertThat(tokenFromDb).isNotNull();
    }
}
