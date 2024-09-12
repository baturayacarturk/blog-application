package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.image.GetAllImagesResponse;
import com.blog.application.blog.dtos.responses.image.UploadedImageResponse;
import com.blog.application.blog.entities.Image;
import com.blog.application.blog.entities.ImageVersion;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.ImageRepository;
import com.blog.application.blog.repositories.ImageVersionRepository;
import com.blog.application.blog.services.image.ImageServiceImpl;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.storage.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageVersionRepository imageVersionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PostService postService;

    @InjectMocks
    private ImageServiceImpl imageService;

    private User mockUser;
    private Post mockPost;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockPost = new Post();
        mockPost.setId(1L);
        mockPost.setUser(mockUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, Arrays.asList())
        );

        tempDir = Files.createTempDirectory("test-images");
        when(fileStorageService.resizeAndSaveImage(any(), anyInt(), anyInt(), anyString()))
                .thenAnswer(invocation -> {
                    String fileName = invocation.getArgument(3);
                    Path filePath = tempDir.resolve(fileName + ".jpg");
                    Files.createFile(filePath);
                    return filePath.toString();
                });
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Handle exception
                    }
                });
    }

    @Test
    public void testUploadImage() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test image content".getBytes());
        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);

        Image mockImage = new Image();
        mockImage.setId(1L);
        when(imageRepository.save(any(Image.class))).thenReturn(mockImage);

        ImageVersion mockVersionResponse = new ImageVersion();
        mockVersionResponse.setId(1L);
        when(imageVersionRepository.save(any(ImageVersion.class))).thenReturn(mockVersionResponse);

        try (MockedStatic<ImageIO> imageIOMock = Mockito.mockStatic(ImageIO.class)) {
            BufferedImage mockBufferedImage = mock(BufferedImage.class);
            when(mockBufferedImage.getWidth()).thenReturn(100);
            when(mockBufferedImage.getHeight()).thenReturn(100);
            imageIOMock.when(() -> ImageIO.read(any(ByteArrayInputStream.class))).thenReturn(mockBufferedImage);

            UploadedImageResponse response = imageService.uploadImage(file, 1L, StorageType.FILE_SYSTEM, Arrays.asList("100x100"));

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(2, response.getImageVersions().size());
        }
    }

    @Test
    public void testRemoveImageFromPost() throws IOException {
        Image image = new Image();
        image.setId(1L);
        image.setStorageType(StorageType.FILE_SYSTEM);
        image.setPost(mockPost);

        Path testFile = tempDir.resolve("test-image.jpg");
        Files.createFile(testFile);

        ImageVersion version = new ImageVersion();
        version.setFilePath(testFile.toString());
        image.setVersions(Arrays.asList(version));

        when(imageRepository.findByIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.of(image));

        imageService.removeImageFromPost(1L, 1L);

        verify(imageRepository, times(1)).delete(image);
    }

    @Test
    public void testGetOriginalImageByImageId() throws IOException {
        Path testFile = tempDir.resolve("original-image.jpg");
        Files.createFile(testFile);

        ImageVersion originalVersion = new ImageVersion();
        originalVersion.setId(1L);
        originalVersion.setFilePath(testFile.toString());

        when(imageVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(originalVersion);

        ResourceResponse result = imageService.getOriginalImageByImageId(1L);

        assertNotNull(result);
        assertNotNull(result.getResource());
        assertTrue(result.getResource().exists());
    }

    @Test
    public void testGetAllImagesByPostId() {
        Image image1 = new Image();
        image1.setId(1L);
        Image image2 = new Image();
        image2.setId(2L);

        ImageVersion version1 = new ImageVersion();
        version1.setId(1L);
        version1.setOriginal(true);
        ImageVersion version2 = new ImageVersion();
        version2.setId(2L);
        version2.setOriginal(false);

        when(imageRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList(image1, image2));
        when(imageVersionRepository.findByImageId(1L)).thenReturn(Arrays.asList(version1, version2));
        when(imageVersionRepository.findByImageId(2L)).thenReturn(Arrays.asList(version1, version2));

        List<GetAllImagesResponse> result = imageService.getAllImagesByPostId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsOriginal());
        assertEquals(1, result.get(0).getImageVersions().size());
    }

    @Test
    public void testGetAllOriginalImagesByPostId() {
        Image image1 = new Image();
        image1.setId(1L);
        Image image2 = new Image();
        image2.setId(2L);

        ImageVersion version1 = new ImageVersion();
        version1.setId(1L);
        version1.setVersionName("original");
        version1.setWidth(100);
        version1.setHeight(100);

        when(imageRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList(image1, image2));
        when(imageVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(version1);

        List<VersionResponse> result = imageService.getAllOriginalImagesByPostId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("original", result.get(0).getVersionName());
        assertEquals(100, result.get(0).getWidth());
        assertEquals(100, result.get(0).getHeight());
    }

    @Test
    public void testGetVersionResponseById() throws IOException {
        Path testFile = tempDir.resolve("version-image.jpg");
        Files.createFile(testFile);

        ImageVersion imageVersion = new ImageVersion();
        imageVersion.setId(1L);
        imageVersion.setFilePath(testFile.toString());

        when(imageVersionRepository.findById(anyLong())).thenReturn(Optional.of(imageVersion));

        ResourceResponse result = imageService.getVersionResponseById(1L);

        assertNotNull(result);
        assertNotNull(result.getResource());
        assertTrue(result.getResource().exists());
    }

    @Test
    public void testUploadImage_PostNotFound() {
        MockMultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test image content".getBytes());
        when(postService.getPostEntity(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            imageService.uploadImage(file, 1L, StorageType.FILE_SYSTEM, Arrays.asList("100x100"));
        });
    }

    @Test
    public void testRemoveImageFromPost_ImageNotFound() {
        when(imageRepository.findByIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            imageService.removeImageFromPost(1L, 1L);
        });
    }

    @Test
    public void testGetOriginalImageByImageId_ImageNotFound() {
        when(imageVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            imageService.getOriginalImageByImageId(1L);
        });
    }

    @Test
    public void testGetAllImagesByPostId_NoImages() {
        when(imageRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList());

        List<GetAllImagesResponse> result = imageService.getAllImagesByPostId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllOriginalImagesByPostId_NoImages() {
        when(imageRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList());

        assertThrows(BusinessException.class, () -> {
            imageService.getAllOriginalImagesByPostId(1L);
        });
    }

    @Test
    public void testGetVersionResponseById_VersionNotFound() {
        when(imageVersionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            imageService.getVersionResponseById(1L);
        });
    }
}