package com.blog.application.blog.testcontainers;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.entities.Video;
import com.blog.application.blog.entities.VideoVersion;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.repositories.VideoRepository;
import com.blog.application.blog.repositories.VideoVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class VideoIntegrationTest extends AbstractContainerBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoVersionRepository videoVersionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Post testPost;
    private String jwtToken;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-videos");
        Files.createDirectories(tempDir);
        videoVersionRepository.deleteAll();
        videoRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setDisplayName("Test User");
        testUser = userRepository.save(testUser);

        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setText("Test Post Text");
        testPost.setUser(testUser);
        testPost = postRepository.save(testPost);

        jwtToken = generateToken("testUser");
        setUpSecurityContext(testUser);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                    }
                });
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testUploadVideo() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "test video content".getBytes());

        ResultActions resultActions = mockMvc.perform(multipart("/api/videos")
                .file(file)
                .param("postId", testPost.getId().toString())
                .param("storageType", StorageType.FILE_SYSTEM.toString())
                .param("qualities", "720p"));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.storageType", is("FILE_SYSTEM")))
                .andExpect(jsonPath("$.videoVersions", hasSize(2)));

        List<Video> savedVideos = videoRepository.findAllByPostId(testPost.getId());
        assertThat(savedVideos).hasSize(1);
        Video savedVideo = savedVideos.get(0);
        assertThat(savedVideo.getFileName()).isEqualTo("test.mp4");
        assertThat(savedVideo.getPost().getId()).isEqualTo(testPost.getId());
        assertThat(savedVideo.getStorageType()).isEqualTo(StorageType.FILE_SYSTEM);

        List<VideoVersion> savedVersions = videoVersionRepository.findByVideoId(savedVideo.getId());
        assertThat(savedVersions).hasSize(2);
        assertThat(savedVersions).extracting(VideoVersion::isOriginal).containsExactlyInAnyOrder(true, false);
    }

    @Test
    public void testRemoveVideoFromPost() throws Exception {
        Video video = new Video();
        video.setFileName("test.mp4");
        video.setPost(testPost);
        video.setStorageType(StorageType.FILE_SYSTEM);
        video = videoRepository.save(video);

        ResultActions resultActions = mockMvc.perform(delete("/api/videos/" + video.getId())
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());

        assertThat(videoRepository.findById(video.getId())).isEmpty();
    }

    @Test
    public void testGetOriginalVideoByVideoId() throws Exception {
        try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.probeContentType(any(Path.class))).thenReturn("video/mp4");
            Video video = new Video();
            video.setFileName("test.mp4");
            video.setPost(testPost);
            video.setStorageType(StorageType.FILE_SYSTEM);
            video = videoRepository.save(video);

            VideoVersion originalVersion = new VideoVersion();
            originalVersion.setVideo(video);
            originalVersion.setOriginal(true);
            originalVersion.setVersionName("original");
            originalVersion.setQuality("1080p");
            originalVersion.setFilePath(tempDir.toString());
            videoVersionRepository.save(originalVersion);

            ResultActions resultActions = mockMvc.perform(get("/api/videos/original")
                    .param("videoId", video.getId().toString())
                    .accept(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                    .andExpect(content().bytes(new byte[]{}));

            VideoVersion savedVersion = videoVersionRepository.findOriginalVersionByVideoId(video.getId());
            assertThat(savedVersion).isNotNull();
            assertThat(savedVersion.isOriginal()).isTrue();
            assertThat(savedVersion.getQuality()).isEqualTo("1080p");
        }
    }

    @Test
    public void testGetVersionResponseById() throws Exception {
        try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.probeContentType(any(Path.class))).thenReturn("video/mp4");

            Video video = new Video();
            video.setFileName("test.mp4");
            video.setPost(testPost);
            video.setStorageType(StorageType.FILE_SYSTEM);
            video = videoRepository.save(video);

            VideoVersion version = new VideoVersion();
            version.setVideo(video);
            version.setOriginal(false);
            version.setVersionName("720p");
            version.setQuality("720p");
            version.setFilePath(tempDir.toString());
            version = videoVersionRepository.save(version);

            ResultActions resultActions = mockMvc.perform(get("/api/videos/version")
                    .param("videoVersionId", version.getId().toString())
                    .accept(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                    .andExpect(content().bytes(new byte[]{}));

            VideoVersion savedVersion = videoVersionRepository.findById(version.getId()).orElse(null);
            assertThat(savedVersion).isNotNull();
            assertThat(savedVersion.isOriginal()).isFalse();
            assertThat(savedVersion.getQuality()).isEqualTo("720p");
        }
    }

    @Test
    public void testGetAllOriginalVideosByPostId() throws Exception {
        Video video1 = createVideoWithOriginalVersion("test1.mp4");
        Video video2 = createVideoWithOriginalVersion("test2.mp4");

        ResultActions resultActions = mockMvc.perform(get("/api/videos/originals")
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versionName", is("original")))
                .andExpect(jsonPath("$[1].versionName", is("original")));

        VideoVersion savedVersion1 = videoVersionRepository.findOriginalVersionByVideoId(video1.getId());
        assertThat(savedVersion1).isNotNull();
        assertThat(savedVersion1.isOriginal()).isTrue();
        assertThat(savedVersion1.getVersionName()).isEqualTo("original");

        VideoVersion savedVersion2 = videoVersionRepository.findOriginalVersionByVideoId(video2.getId());
        assertThat(savedVersion2).isNotNull();
        assertThat(savedVersion2.isOriginal()).isTrue();
        assertThat(savedVersion2.getVersionName()).isEqualTo("original");
    }

    @Test
    public void testGetAllVideosByPostId() throws Exception {
        createVideoWithOriginalVersion("test1.mp4");
        createVideoWithOriginalVersion("test2.mp4");

        ResultActions resultActions = mockMvc.perform(get("/api/videos/findAll")
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isOriginal", is(true)))
                .andExpect(jsonPath("$[1].isOriginal", is(true)));

        List<Video> savedVideos = videoRepository.findAllByPostId(testPost.getId());
        assertThat(savedVideos).hasSize(2);
        for (Video video : savedVideos) {
            VideoVersion originalVersion = videoVersionRepository.findOriginalVersionByVideoId(video.getId());
            assertThat(originalVersion).isNotNull();
            assertThat(originalVersion.isOriginal()).isTrue();
        }
    }

    private Video createVideoWithOriginalVersion(String fileName) {
        Video video = new Video();
        video.setFileName(fileName);
        video.setPost(testPost);
        video.setStorageType(StorageType.FILE_SYSTEM);
        video = videoRepository.save(video);

        VideoVersion originalVersion = new VideoVersion();
        originalVersion.setVideo(video);
        originalVersion.setOriginal(true);
        originalVersion.setVersionName("original");
        originalVersion.setQuality("1080p");
        originalVersion.setFilePath("somepath");
        videoVersionRepository.save(originalVersion);

        return video;
    }

    private static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "47A52F686696CABA4A9824E6177DFFFF5161ASDFDS1D2DS")
                .compact();
    }

    private void setUpSecurityContext(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
