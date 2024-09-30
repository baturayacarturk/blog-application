package com.blog.application.blog.repository;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Video;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.VideoRepository;
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
public class VideoRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;

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
        Video video = new Video();
        video.setFileName("test.mp4");
        video.setPost(testPost);
        video.setStorageType(StorageType.FILE_SYSTEM);
        video = videoRepository.save(video);

        Optional<Video> found = videoRepository.findByIdAndPostId(video.getId(), testPost.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("test.mp4");
    }

    @Test
    public void testFindAllByPostId() {
        Video video1 = new Video();
        video1.setFileName("test1.mp4");
        video1.setPost(testPost);
        video1.setStorageType(StorageType.FILE_SYSTEM);
        videoRepository.save(video1);

        Video video2 = new Video();
        video2.setFileName("test2.mp4");
        video2.setPost(testPost);
        video2.setStorageType(StorageType.FILE_SYSTEM);
        videoRepository.save(video2);

        List<Video> videos = videoRepository.findAllByPostId(testPost.getId());

        assertThat(videos).hasSize(2);
        assertThat(videos).extracting(Video::getFileName).containsExactlyInAnyOrder("test1.mp4", "test2.mp4");
    }

    @Test
    public void testFindByIdAndPostId_NotFound() {
        Optional<Video> notFound = videoRepository.findByIdAndPostId(999L, testPost.getId());

        assertThat(notFound).isEmpty();
    }

    @Test
    public void testFindAllByPostId_NoVideos() {
        List<Video> emptyList = videoRepository.findAllByPostId(testPost.getId());

        assertThat(emptyList).isEmpty();
    }

    @Test
    public void testFindAllByPostId_MultiplePostsWithVideos() {
        Post anotherPost = new Post();
        anotherPost.setTitle("Another Test Post");
        anotherPost = postRepository.save(anotherPost);

        Video video1 = new Video();
        video1.setFileName("test1.mp4");
        video1.setPost(testPost);
        video1.setStorageType(StorageType.FILE_SYSTEM);
        videoRepository.save(video1);

        Video video2 = new Video();
        video2.setFileName("test2.mp4");
        video2.setPost(anotherPost);
        video2.setStorageType(StorageType.FILE_SYSTEM);
        videoRepository.save(video2);

        List<Video> videosForTestPost = videoRepository.findAllByPostId(testPost.getId());
        List<Video> videosForAnotherPost = videoRepository.findAllByPostId(anotherPost.getId());

        assertThat(videosForTestPost).hasSize(1);
        assertThat(videosForTestPost.get(0).getFileName()).isEqualTo("test1.mp4");

        assertThat(videosForAnotherPost).hasSize(1);
        assertThat(videosForAnotherPost.get(0).getFileName()).isEqualTo("test2.mp4");
    }
}