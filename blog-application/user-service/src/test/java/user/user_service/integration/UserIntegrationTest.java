package user.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import user.user_service.dtos.AuthenticationDto;
import user.user_service.entities.Token;
import user.user_service.entities.User;
import user.user_service.repositories.TokenRepository;
import user.user_service.repositories.UserRepository;
import user.user_service.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserIntegrationTest {

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
        AuthenticationDto registerRequest = new AuthenticationDto();
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
        AuthenticationDto registerRequest = new AuthenticationDto();
        registerRequest.setUsername("testUser");
        registerRequest.setPassword("password");
        userService.register(registerRequest);

        AuthenticationDto authenticationRequest = new AuthenticationDto();
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
