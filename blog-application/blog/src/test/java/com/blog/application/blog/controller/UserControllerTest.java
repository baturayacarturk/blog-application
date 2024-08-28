package com.blog.application.blog.controller;


import com.blog.application.blog.controllers.UserController;
import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.jwt.config.JwtService;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@ComponentScan(basePackages = {"com.blog.application.blog.jwt"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {UserController.class, UserService.class})

public class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenRepository tokenRepository;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john_doe");
        request.setPassword("password123");

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("dummy-token");

        when(userService.register(request)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is(response.getToken())));
    }

    @Test
    void testAuthenticate() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("john_doe");
        request.setPassword("password123");

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("dummy-token");

        when(userService.authenticate(request)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(response.getToken())));
    }
}
