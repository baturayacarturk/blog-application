package com.blog.application.blog.repositories;

import com.blog.application.blog.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByIdAndPostId(Long id, Long postId);
    List<Video> findAllByPostId(Long postId);

}