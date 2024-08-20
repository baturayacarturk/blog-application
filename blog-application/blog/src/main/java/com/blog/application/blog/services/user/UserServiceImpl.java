package com.blog.application.blog.services.user;

import com.blog.application.blog.repositories.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
