package com.blog.application.blog.repository;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TagRepository tagRepository;
    private Long tag1Id;
    private Long tag2Id;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        Tag tag1 = new Tag();
        tag1.setName("Technology");
        Tag tagResponse = tagRepository.save(tag1);
        tag1Id = tagResponse.getId();

        Tag tag2 = new Tag();
        tag2.setName("Health");
        Tag tagResponse2 = tagRepository.save(tag2);
        tag2Id = tagResponse2.getId();

        Post post1 = new Post();
        post1.setTitle("Tech Post");
        post1.setText("A post about technology.");
        post1.setTags(new HashSet<>(Set.of(tag1)));
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Health Post");
        post2.setText("A post about health.");
        post2.setTags(new HashSet<>(Set.of(tag2)));
        postRepository.save(post2);

    }


    @Test
    public void testGetAllSimplifiedBlogPost() {
        List<SimplifiedPostProjection> projections = postRepository.getAllSimplifiedBlogPost();

        assertNotNull(projections);
        assertEquals(2, projections.size());

        assertEquals("Tech Post", projections.get(0).getTitle());
        assertEquals("A post about technology.", projections.get(0).getText());

        assertEquals("Health Post", projections.get(1).getTitle());
        assertEquals("A post about health.", projections.get(1).getText());

    }

    @Test
    public void testGetPostEntity() {
        Post post = postRepository.getPostEntity(1L);

        assertNotNull(post);
        assertEquals("Tech Post", post.getTitle());
        assertEquals("A post about technology.", post.getText());
        assertEquals(1, post.getTags().size());
        assertTrue(post.getTags().stream().anyMatch(tag -> tag.getName().equals("Technology")));
    }

    @Test
    public void testGetAllPostEntitites() {
        List<Post> posts = postRepository.getAllPostEntitites();

        assertNotNull(posts);
        assertEquals(2, posts.size());

        assertEquals("Tech Post", posts.get(0).getTitle());
        assertEquals("Health Post", posts.get(1).getTitle());

        assertTrue(posts.get(0).getTags().stream().anyMatch(tag -> tag.getName().equals("Technology")));
        assertTrue(posts.get(1).getTags().stream().anyMatch(tag -> tag.getName().equals("Health")));
    }

    @Test
    public void testGetPostEntityByTagId() {
        List<Post> techPosts = postRepository.getPostEntityByTagId(tag1Id);
        List<Post> healthPosts = postRepository.getPostEntityByTagId(tag2Id);

        assertNotNull(techPosts);
        assertFalse(techPosts.isEmpty());
        assertEquals(1, techPosts.size());
        assertNotNull(healthPosts);
        assertFalse(healthPosts.isEmpty());
        assertEquals(1, healthPosts.size());
    }
}
