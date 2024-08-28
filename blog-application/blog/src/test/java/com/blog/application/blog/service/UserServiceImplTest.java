package com.blog.application.blog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.entities.Token;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.jwt.config.JwtService;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenRepository tokenRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setDisplayName("New User");

        User user = new User();
        user.setUsername("newuser");
        user.setPassword("encodedPassword");
        user.setDisplayName("New User");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        AuthenticationResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testRegister_UsernameTaken() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password");
        registerRequest.setDisplayName("Existing User");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Username is already taken.", thrown.getMessage());
        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testAuthenticate_Success() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        AuthenticationResponse response = userService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository, times(1)).findByUsername("user");
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername("nonexistentuser");
        authRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userService.authenticate(authRequest);
        });

        assertEquals("User not found", thrown.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistentuser");
        verify(jwtService, times(0)).generateToken(any(User.class));
        verify(tokenRepository, times(0)).save(any(Token.class));
    }

    @Test
    public void testAuthenticateShouldRevokeAllUserTokens() {
        User user = new User();
        user.setId(1L);

        Token token1 = new Token();
        token1.setExpired(false);
        token1.setRevoked(false);

        Token token2 = new Token();
        token2.setExpired(false);
        token2.setRevoked(false);

        List<Token> tokens = List.of(token1, token2);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllValidTokensByUser(1L)).thenReturn(tokens);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = userService.authenticate(authenticationRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());

        verify(tokenRepository, times(1)).findAllValidTokensByUser(1L);
        verify(tokenRepository, times(1)).saveAll(tokens);

        assertTrue(token1.isExpired());
        assertTrue(token1.isRevoked());
        assertTrue(token2.isExpired());
        assertTrue(token2.isRevoked());
    }
}