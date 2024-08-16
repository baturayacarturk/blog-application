package com.blog.application.blog.controllers;
import com.blog.application.blog.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;



@RestController
@RequestMapping(path = "/api/users",produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class UserController {
    private UserRepository userRepository;
}
