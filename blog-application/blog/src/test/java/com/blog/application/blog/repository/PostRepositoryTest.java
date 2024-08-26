package com.blog.application.blog.repository;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        Post post = new Post();
        post.setTitle("Test Title");
        post.setText("Test Text");
        entityManager.persist(post);
        entityManager.flush();
    }

    @Test
    void testGetAllSimplifiedBlogPost() {
        List<SimplifiedPostProjection> posts = postRepository.getAllSimplifiedBlogPost();

        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0).getTitle()).isEqualTo("Test Title");
        assertThat(posts.get(0).getText()).isEqualTo("Test Text");
    }

    @Test
    void testGetPostEntity() {
        Post post = new Post();
        post.setTitle("Test Title");
        post.setText("Test Text");
        entityManager.persist(post);
        entityManager.flush();

        Post foundPost = postRepository.getPostEntity(post.getId());

        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getTitle()).isEqualTo("Test Title");
        assertThat(foundPost.getText()).isEqualTo("Test Text");
    }

    @Test
    void testGetPostEntityByTagId() {

        Tag tag = new Tag();
        tag.setName("Test Tag");
        entityManager.persist(tag);
        entityManager.flush();

        Post post = new Post();
        post.setTitle("Test Title");
        post.setText("Test Text");
        post.setTags(new HashSet<>(Collections.singletonList(tag)));
        entityManager.persist(post);
        entityManager.flush();

        List<Post> posts = postRepository.getPostEntityByTagId(tag.getId());

        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0)).isEqualTo(post);
    }


}
