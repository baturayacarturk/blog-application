package com.blog.application.blog.repository;
import com.blog.application.blog.entities.Image;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.repositories.ImageRepository;
import com.blog.application.blog.repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PostRepository postRepository;

    private Post testPost;

    @BeforeEach
    public void setUp() {
        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost = postRepository.save(testPost);
    }

    @Test
    public void testFindByIdAndPostId() {
        Image image = new Image();
        image.setFileName("test.jpg");
        image.setPost(testPost);
        image.setStorageType(StorageType.FILE_SYSTEM);
        image = entityManager.persist(image);

        Optional<Image> found = imageRepository.findByIdAndPostId(image.getId(), testPost.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("test.jpg");
    }

    @Test
    public void testFindAllByPostId() {
        Image image1 = new Image();
        image1.setFileName("test1.jpg");
        image1.setPost(testPost);
        image1.setStorageType(StorageType.FILE_SYSTEM);
        entityManager.persist(image1);

        Image image2 = new Image();
        image2.setFileName("test2.jpg");
        image2.setPost(testPost);
        image2.setStorageType(StorageType.FILE_SYSTEM);
        entityManager.persist(image2);

        List<Image> images = imageRepository.findAllByPostId(testPost.getId());

        assertThat(images).hasSize(2);
        assertThat(images).extracting(Image::getFileName).containsExactlyInAnyOrder("test1.jpg", "test2.jpg");
    }

}