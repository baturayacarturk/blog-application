package com.blog.application.blog.testcontainers;

import com.blog.application.blog.dtos.responses.client.UserClientDto;
import com.blog.application.blog.entities.Image;
import com.blog.application.blog.entities.ImageVersion;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.repositories.ImageRepository;
import com.blog.application.blog.repositories.ImageVersionRepository;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.services.client.UserFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class ImageIntegrationTest extends AbstractContainerBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageVersionRepository imageVersionRepository;

    @Autowired
    private PostRepository postRepository;
    @MockBean
    private UserFeignClient userFeignClient;

    private Post testPost;
    private Path tempDir;


    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-images");
        Files.createDirectories(tempDir);
        imageVersionRepository.deleteAll();
        imageRepository.deleteAll();
        postRepository.deleteAll();

        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setText("Test Post Text");
        testPost.setUserId(10L);
        testPost = postRepository.save(testPost);
        UserClientDto mockUser = new UserClientDto();
        mockUser.setId(10L);
        when(userFeignClient.getUserDetails()).thenReturn(ResponseEntity.ok(mockUser));

    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(tempDir)
                .map(Path::toFile)
                .forEach(file -> file.delete());
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        try (MockedStatic<ImageIO> imageIOMock = Mockito.mockStatic(ImageIO.class)) {
            BufferedImage mockBufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            imageIOMock.when(() -> ImageIO.read(any(ByteArrayInputStream.class))).thenReturn(mockBufferedImage);

            ResultActions resultActions = mockMvc.perform(multipart("/api/images")
                    .file(file)
                    .param("postId", testPost.getId().toString())
                    .param("storageType", StorageType.FILE_SYSTEM.toString())
                    .param("sizes", "100x100"));

            resultActions.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.storageType", is("FILE_SYSTEM")))
                    .andExpect(jsonPath("$.imageVersions", hasSize(2)));

            List<Image> savedImages = imageRepository.findAllByPostId(testPost.getId());
            assertThat(savedImages).hasSize(1);
            Image savedImage = savedImages.get(0);
            assertThat(savedImage.getFileName()).isEqualTo("test.jpg");
            assertThat(savedImage.getPost().getId()).isEqualTo(testPost.getId());
            assertThat(savedImage.getStorageType()).isEqualTo(StorageType.FILE_SYSTEM);

            List<ImageVersion> savedVersions = imageVersionRepository.findByImageId(savedImage.getId());
            assertThat(savedVersions).hasSize(2);
            assertThat(savedVersions).extracting(ImageVersion::isOriginal).containsExactlyInAnyOrder(true, false);
        }
    }

    @Test
    public void testRemoveImageFromPost() throws Exception {
        Image image = new Image();
        image.setFileName("test.jpg");
        image.setPost(testPost);
        image.setStorageType(StorageType.FILE_SYSTEM);
        image = imageRepository.save(image);

        ResultActions resultActions = mockMvc.perform(delete("/api/images/" + image.getId())
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());

        assertThat(imageRepository.findById(image.getId())).isEmpty();
    }

    @Test
    public void testGetOriginalImageByImageId() throws Exception {
        try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.probeContentType(any(Path.class))).thenReturn("image/jpeg");
            Image image = new Image();
            image.setFileName("test.jpg");
            image.setPost(testPost);
            image.setStorageType(StorageType.FILE_SYSTEM);
            image = imageRepository.save(image);

            ImageVersion originalVersion = new ImageVersion();
            originalVersion.setImage(image);
            originalVersion.setOriginal(true);
            originalVersion.setVersionName("original");
            originalVersion.setWidth(1000);
            originalVersion.setHeight(800);
            originalVersion.setFilePath(tempDir.toString());
            imageVersionRepository.save(originalVersion);

            ResultActions resultActions = mockMvc.perform(get("/api/images/original")
                    .param("imageId", image.getId().toString())
                    .accept(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                    .andExpect(content().bytes(new byte[]{}));

            ImageVersion savedVersion = imageVersionRepository.findOriginalVersionByVideoId(image.getId());
            assertThat(savedVersion).isNotNull();
            assertThat(savedVersion.isOriginal()).isTrue();
            assertThat(savedVersion.getWidth()).isEqualTo(1000);
            assertThat(savedVersion.getHeight()).isEqualTo(800);
        }
    }

    @Test
    public void testGetVersionResponseById() throws Exception {
        try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.probeContentType(any(Path.class))).thenReturn("image/jpeg");

            Image image = new Image();
            image.setFileName("test.jpg");
            image.setPost(testPost);
            image.setStorageType(StorageType.FILE_SYSTEM);
            image = imageRepository.save(image);

            ImageVersion version = new ImageVersion();
            version.setImage(image);
            version.setOriginal(false);
            version.setVersionName("100x100");
            version.setWidth(100);
            version.setHeight(100);
            version.setFilePath(tempDir.toString());
            version = imageVersionRepository.save(version);

            ResultActions resultActions = mockMvc.perform(get("/api/images/version")
                    .param("imageVersionId", version.getId().toString())
                    .accept(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                    .andExpect(content().bytes(new byte[]{}));

            ImageVersion savedVersion = imageVersionRepository.findById(version.getId()).orElse(null);
            assertThat(savedVersion).isNotNull();
            assertThat(savedVersion.isOriginal()).isFalse();
            assertThat(savedVersion.getWidth()).isEqualTo(100);
            assertThat(savedVersion.getHeight()).isEqualTo(100);
        }
    }

    @Test
    public void testGetAllOriginalImagesByPostId() throws Exception {
        Image image1 = createImageWithOriginalVersion("test1.jpg");
        Image image2 = createImageWithOriginalVersion("test2.jpg");

        ResultActions resultActions = mockMvc.perform(get("/api/images/originals")
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versionName", is("original")))
                .andExpect(jsonPath("$[1].versionName", is("original")));

        ImageVersion savedVersion1 = imageVersionRepository.findOriginalVersionByVideoId(image1.getId());
        assertThat(savedVersion1).isNotNull();
        assertThat(savedVersion1.isOriginal()).isTrue();
        assertThat(savedVersion1.getVersionName()).isEqualTo("original");

        ImageVersion savedVersion2 = imageVersionRepository.findOriginalVersionByVideoId(image2.getId());
        assertThat(savedVersion2).isNotNull();
        assertThat(savedVersion2.isOriginal()).isTrue();
        assertThat(savedVersion2.getVersionName()).isEqualTo("original");
    }

    @Test
    public void testGetAllImagesByPostId() throws Exception {
        createImageWithOriginalVersion("test1.jpg");
        createImageWithOriginalVersion("test2.jpg");

        ResultActions resultActions = mockMvc.perform(get("/api/images/findAll")
                .param("postId", testPost.getId().toString())
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isOriginal", is(true)))
                .andExpect(jsonPath("$[1].isOriginal", is(true)));

        List<Image> savedImages = imageRepository.findAllByPostId(testPost.getId());
        assertThat(savedImages).hasSize(2);
        for (Image image : savedImages) {
            ImageVersion originalVersion = imageVersionRepository.findOriginalVersionByVideoId(image.getId());
            assertThat(originalVersion).isNotNull();
            assertThat(originalVersion.isOriginal()).isTrue();
        }
    }

    private Image createImageWithOriginalVersion(String fileName) {
        Image image = new Image();
        image.setFileName(fileName);
        image.setPost(testPost);
        image.setStorageType(StorageType.FILE_SYSTEM);
        image = imageRepository.save(image);

        ImageVersion originalVersion = new ImageVersion();
        originalVersion.setImage(image);
        originalVersion.setOriginal(true);
        originalVersion.setVersionName("original");
        originalVersion.setWidth(1000);
        originalVersion.setHeight(800);
        originalVersion.setFilePath("somepath");
        imageVersionRepository.save(originalVersion);

        return image;
    }

}