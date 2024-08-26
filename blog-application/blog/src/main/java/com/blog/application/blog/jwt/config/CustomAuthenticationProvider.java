package com.blog.application.blog.jwt.config;

import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()) throw new BusinessException("User not found");
        if(!validatePassword(password,user.get().getPassword())){
            throw new BadCredentialsException("Invalid password");
        }
        return new UsernamePasswordAuthenticationToken(user,password,null);
    }

    private boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword,encodedPassword);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
