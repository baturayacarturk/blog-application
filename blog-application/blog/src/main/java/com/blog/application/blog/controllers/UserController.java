package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.requests.user.AuthenticationRequest;
import com.blog.application.blog.dtos.requests.user.RegisterRequest;
import com.blog.application.blog.dtos.responses.user.AuthenticationResponse;
import com.blog.application.blog.services.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Api(value = "User Management", tags = "Users")
public class UserController {

    private final UserService userService;

    /**
     * Registers a new user and returns an authentication response with a JWT token.
     *
     * @param registerRequest the details of the user to be registered
     * @return a {@link ResponseEntity} containing the authentication response with JWT token
     */
    @ApiOperation(value = "Register a new user",
            notes = "Registers a new user and returns an authentication response with a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User registered successfully", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Invalid input")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @ApiParam(value = "Details of the user to be registered", required = true)
            @RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse authenticationResponse = userService.register(registerRequest);
        return new ResponseEntity<>(authenticationResponse, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user with their credentials and returns an authentication response with a JWT token.
     *
     * @param authenticationRequest the credentials of the user to be authenticated
     * @return a {@link ResponseEntity} containing the authentication response with JWT token
     */
    @ApiOperation(value = "Authenticate a user",
            notes = "Authenticates a user with their credentials and returns an authentication response with a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User authenticated successfully", response = AuthenticationResponse.class),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @ApiParam(value = "Credentials of the user to be authenticated", required = true)
            @RequestBody AuthenticationRequest authenticationRequest) {
        AuthenticationResponse authenticationResponse = userService.authenticate(authenticationRequest);
        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
    }
}
