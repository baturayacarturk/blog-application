package com.blog.application.blog.services.user;

import com.blog.application.blog.dtos.common.UserDto;
import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.entities.Token;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.jwt.config.JwtService;
import com.blog.application.blog.repositories.TokenRepository;
import com.blog.application.blog.repositories.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        var user = new User();
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if(existingUser.isPresent()){
            logger.error("Username is already taken with follows {}",registerRequest.getUsername());
            throw new BusinessException("Username is already taken.");
        }
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName());
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(jwtToken);
        return authenticationResponse;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        var user = userRepository.findByUsername(authenticationRequest.getUsername());
        if (user.isEmpty()) {
            logger.error("User not found with username follows {}",authenticationRequest.getUsername());
            throw new BusinessException("User not found");
        }
        var jwtToken = jwtService.generateToken(user.get());
        revokeAllUserTokens(user.get());
        saveUserToken(user.get(), jwtToken);

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
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
        Optional<User> user =  userRepository.findByUsername(username);
        return user;
    }

    @Override
    public Optional<UserDto> findOnlyUserById(Long userId) {
        //TODO can be added validation and return without optional
        return userRepository.findOnlyUser(userId);
    }

    private void revokeAllUserTokens(User user){
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if(validTokens.isEmpty()){
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
