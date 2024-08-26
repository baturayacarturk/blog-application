package com.blog.application.blog.repositories;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface PostRepository extends JpaRepository<Post,Long> {
    @Query("SELECT p.title AS title, p.text AS text FROM Post p")
    List<SimplifiedPostProjection> getAllSimplifiedBlogPost();
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id = :postId")
    Post getPostEntity(Long postId);
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    List<Post> getPostEntityByTagId(Long tagId);
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags")
    List<Post> getAllPostEntitites();

}
