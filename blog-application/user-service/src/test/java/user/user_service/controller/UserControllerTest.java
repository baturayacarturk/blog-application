package user.user_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import user.user_service.dtos.AuthenticationDto;
import user.user_service.dtos.UserDto;
import user.user_service.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister() throws Exception {
        AuthenticationDto registerRequest = new AuthenticationDto();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");

        AuthenticationDto authenticationResponse = new AuthenticationDto();
        authenticationResponse.setToken("dummyToken");

        when(userService.register(any(AuthenticationDto.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"token\":\"dummyToken\"}"));

        verify(userService, times(1)).register(any(AuthenticationDto.class));
    }

    @Test
    void testAuthenticate() throws Exception {
        AuthenticationDto authRequest = new AuthenticationDto();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");

        AuthenticationDto authenticationResponse = new AuthenticationDto();
        authenticationResponse.setToken("dummyToken");

        when(userService.authenticate(any(AuthenticationDto.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/users/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"token\":\"dummyToken\"}"));

        verify(userService, times(1)).authenticate(any(AuthenticationDto.class));
    }

    @Test
    void testGetUserDetails() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");

        when(userService.getUserDetails()).thenReturn(userDto);

        mockMvc.perform(get("/api/users/getUserDetails"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":1,\"username\":\"testuser\"}"));

        verify(userService, times(1)).getUserDetails();
    }
}