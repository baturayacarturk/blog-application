package com.blog.application.blog.helpers.params.utils;

import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.types.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static User extractUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof Optional<?>) {
            Optional<?> optionalPrincipal = (Optional<?>) principal;
            if (optionalPrincipal.isPresent() && optionalPrincipal.get() instanceof User) {
                return (User) optionalPrincipal.get();
            } else {
                throw new IllegalStateException("Unexpected principal type");
            }
        } else {
            throw new BusinessException("User not found. Please login or register");
        }
    }
}