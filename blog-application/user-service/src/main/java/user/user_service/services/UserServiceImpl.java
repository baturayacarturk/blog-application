package user.user_service.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user.user_service.dtos.AuthenticationDto;
import user.user_service.dtos.UserDto;
import user.user_service.entities.Token;
import user.user_service.entities.User;
import user.user_service.exceptions.types.BusinessException;
import user.user_service.jwt.JwtService;
import user.user_service.repositories.TokenRepository;
import user.user_service.repositories.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    @Override
    public AuthenticationDto register(AuthenticationDto registerRequest) {
        var user = new User();
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            logger.error("Username is already taken with follows {}", registerRequest.getUsername());
            throw new AuthenticationCredentialsNotFoundException("Username is already taken.");
        }
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName());
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        AuthenticationDto authenticationResponse = new AuthenticationDto();
        authenticationResponse.setToken(jwtToken);
        return authenticationResponse;
    }

    @Override
    public AuthenticationDto authenticate(AuthenticationDto authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        var user = userRepository.findByUsername(authenticationRequest.getUsername());
        if (user.isEmpty()) {
            logger.error("User not found with username follows {}", authenticationRequest.getUsername());
        }
        var jwtToken = jwtService.generateToken(user.get());
        revokeAllUserTokens(user.get());
        saveUserToken(user.get(), jwtToken);

        AuthenticationDto authenticationResponse = new AuthenticationDto();
        authenticationResponse.setToken(jwtToken);
        return authenticationResponse;
    }


    @Override
    public Optional<User> findByUserId(Long userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user;
    }

    @Override
    public Optional<UserDto> findOnlyUserById(Long userId) {
        //TODO can be added validation and return without optional
        return userRepository.findOnlyUser(userId);
    }

    @Override
    public UserDto getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        User user;
        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof Optional<?>) {
            Optional<?> optionalPrincipal = (Optional<?>) principal;
            if (optionalPrincipal.isPresent() && optionalPrincipal.get() instanceof User) {
                user = (User) optionalPrincipal.get();
            } else {
                throw new BusinessException("User not found");
            }
        } else {
            throw new BusinessException("User not found");
        }
        var currentUser = findByUsername(user.getUsername());
        if (currentUser.isEmpty()) {
            throw new BusinessException("User not found");
        }
        UserDto userDto = new UserDto();
        userDto.setId(currentUser.get().getId());
        userDto.setUsername(currentUser.get().getUsername());
        return userDto;
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = new Token();
        token.setUser(user);
        token.setToken(jwtToken);
        token.setRevoked(false);
        token.setExpired(false);
        tokenRepository.save(token);
    }


}
