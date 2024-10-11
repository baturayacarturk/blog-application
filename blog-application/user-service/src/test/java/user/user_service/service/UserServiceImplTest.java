package user.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import user.user_service.dtos.AuthenticationDto;
import user.user_service.dtos.UserDto;
import user.user_service.entities.Token;
import user.user_service.entities.User;
import user.user_service.exceptions.types.BusinessException;
import user.user_service.jwt.JwtService;
import user.user_service.repositories.TokenRepository;
import user.user_service.repositories.UserRepository;
import user.user_service.services.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
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
        AuthenticationDto registerRequest = new AuthenticationDto();
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

        AuthenticationDto response = userService.register(registerRequest);

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
        AuthenticationDto registerRequest = new AuthenticationDto();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password");
        registerRequest.setDisplayName("Existing User");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        AuthenticationCredentialsNotFoundException thrown = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Username is already taken.", thrown.getMessage());
        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testAuthenticate_Success() {
        AuthenticationDto authRequest = new AuthenticationDto();
        authRequest.setUsername("user");
        authRequest.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        AuthenticationDto response = userService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository, times(1)).findByUsername("user");
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        AuthenticationDto authRequest = new AuthenticationDto();
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

        AuthenticationDto authenticationRequest = new AuthenticationDto();
        authenticationRequest.setUsername("username");
        authenticationRequest.setPassword("password");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllValidTokensByUser(1L)).thenReturn(tokens);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationDto response = userService.authenticate(authenticationRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());

        verify(tokenRepository, times(1)).findAllValidTokensByUser(1L);
        verify(tokenRepository, times(1)).saveAll(tokens);

        assertTrue(token1.isExpired());
        assertTrue(token1.isRevoked());
        assertTrue(token2.isExpired());
        assertTrue(token2.isRevoked());
    }

    @Test
    public void testFindOnlyUserById_UserExists() {
        Long userId = 1L;
        UserDto mockUserDto = new UserDto();
        mockUserDto.setId(userId);
        mockUserDto.setUsername("testUser");
        when(userRepository.findOnlyUser(userId)).thenReturn(Optional.of(mockUserDto));

        Optional<UserDto> result = userService.findOnlyUserById(userId);

        assertEquals(Optional.of(mockUserDto), result);
    }
}
