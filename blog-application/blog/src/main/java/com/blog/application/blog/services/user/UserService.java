package com.blog.application.blog.services.user;

import com.blog.application.blog.dtos.common.UserDto;
import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.entities.User;

import java.util.Optional;

public interface UserService {
    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse authenticate (AuthenticationRequest authenticationRequest);
    Optional<User> findByUserId(Long userId);
    Optional<User> findByUsername(String username);
    //Without loading entire User but only user
    Optional<UserDto> findOnlyUserById(Long userId);

}
