package com.blog.application.blog.repositories;

import com.blog.application.blog.entities.Image;
import com.blog.application.blog.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByIdAndPostId(Long id, Long postId);
    List<Image> findAllByPostId(Long postId);
}