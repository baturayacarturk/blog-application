package com.blog.application.blog.repositories;

import com.blog.application.blog.dtos.common.UserDto;
import com.blog.application.blog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM User u WHERE u.username =:username")
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.id =:userId")
    Optional<User> findByUserId(Long userId);
    //Do not bring other relations
    @Query("SELECT new com.blog.application.blog.dtos.common.UserDto(u.id, u.username) FROM User u WHERE u.id = :userId")
    Optional<UserDto> findOnlyUser(Long userId);
}
