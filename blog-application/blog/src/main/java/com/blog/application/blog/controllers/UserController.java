package com.blog.application.blog.controllers;
import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;

import com.blog.application.blog.services.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;



@RestController
@RequestMapping(path = "/api/users",produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest){
        AuthenticationResponse authenticationResponse = userService.register(registerRequest);
        return new ResponseEntity<>(authenticationResponse, HttpStatus.CREATED);

    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest){
        AuthenticationResponse authenticationResponse = userService.authenticate(authenticationRequest);
        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
    }

}
