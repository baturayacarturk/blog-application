package com.blog.application.blog.services.user;

import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.jwt.config.JwtService;
import com.blog.application.blog.repositories.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        var user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName());
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
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
            throw new BusinessException("User not found");
        }
        var jwtToken = jwtService.generateToken(user.get());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(jwtToken);
        return authenticationResponse;
    }

    @Override
    public User findByUserId(Long userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isEmpty()){
            throw new BusinessException("User not found");
        }
        return user.get();
    }

}
