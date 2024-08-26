package com.blog.application.blog.services.user;

import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.entities.User;

public interface UserService {
    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse authenticate (AuthenticationRequest authenticationRequest);
    User findByUserId(Long userId);
}
