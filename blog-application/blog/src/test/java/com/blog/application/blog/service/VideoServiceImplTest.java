package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.client.UserClientDto;
import com.blog.application.blog.dtos.responses.video.GetAllVideosResponse;
import com.blog.application.blog.dtos.responses.video.UploadedVideoResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Video;
import com.blog.application.blog.entities.VideoVersion;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.VideoRepository;
import com.blog.application.blog.repositories.VideoVersionRepository;
import com.blog.application.blog.services.client.UserFeignClient;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.storage.FileStorageService;
import com.blog.application.blog.services.video.VideoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class VideoServiceImplTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoVersionRepository videoVersionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PostService postService;
    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private VideoServiceImpl videoService;

    private Post mockPost;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockPost = new Post();
        mockPost.setId(1L);
        mockPost.setUserId(10L);
        UserClientDto mockUserClientDto = new UserClientDto();
        mockUserClientDto.setId(10L);

        when(userFeignClient.getUserDetails()).thenReturn(ResponseEntity.ok(mockUserClientDto));

        tempDir = Files.createTempDirectory("test-videos");
        when(fileStorageService.saveVideoWithQuality(any(), anyString()))
                .thenAnswer(invocation -> {
                    String quality = invocation.getArgument(1);
                    Path filePath = tempDir.resolve("video_" + quality + ".mp4");
                    Files.createFile(filePath);
                    return filePath.toString();
                });
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
    }

    @Test
    public void testUploadVideo() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.mp4", "test.mp4", "video/mp4", "test video content".getBytes());
        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);

        Video mockVideo = new Video();
        mockVideo.setId(1L);
        when(videoRepository.save(any(Video.class))).thenReturn(mockVideo);

        VideoVersion mockVersionResponse = new VideoVersion();
        mockVersionResponse.setId(1L);
        when(videoVersionRepository.save(any(VideoVersion.class))).thenReturn(mockVersionResponse);

        UploadedVideoResponse response = videoService.uploadVideo(file, 1L, StorageType.FILE_SYSTEM, Arrays.asList("720p"));

        assertNotNull(response);
        assertEquals(1L, response.getVideoVersions().get(0).getId());
        assertEquals(2, response.getVideoVersions().size());
    }

    @Test
    public void testRemoveVideoFromPost() throws IOException {
        Video video = new Video();
        video.setId(1L);
        video.setStorageType(StorageType.FILE_SYSTEM);
        video.setPost(mockPost);

        Path testFile = tempDir.resolve("test-video.mp4");
        Files.createFile(testFile);

        VideoVersion version = new VideoVersion();
        version.setFilePath(testFile.toString());
        video.setVersions(Arrays.asList(version));

        when(videoRepository.findByIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.of(video));
        doNothing().when(fileStorageService).deleteFile(anyString());

        videoService.removeVideoFromPost(1L, 1L);

        verify(videoRepository, times(1)).delete(video);
        verify(fileStorageService, times(1)).deleteFile(testFile.toString());
    }

    @Test
    public void testGetOriginalVideoByVideoId() throws IOException {
        Path testFile = tempDir.resolve("original-video.mp4");
        Files.createFile(testFile);

        VideoVersion originalVersion = new VideoVersion();
        originalVersion.setId(1L);
        originalVersion.setFilePath(testFile.toString());

        when(videoVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(originalVersion);

        ResourceResponse result = videoService.getOriginalVideoByVideoId(1L);

        assertNotNull(result);
        assertNotNull(result.getResource());
        assertTrue(result.getResource().exists());
    }

    @Test
    public void testGetAllVideosByPostId() {
        Video video1 = new Video();
        video1.setId(1L);
        Video video2 = new Video();
        video2.setId(2L);

        VideoVersion version1 = new VideoVersion();
        version1.setId(1L);
        version1.setOriginal(true);
        VideoVersion version2 = new VideoVersion();
        version2.setId(2L);
        version2.setOriginal(false);

        when(videoRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList(video1, video2));
        when(videoVersionRepository.findByVideoId(1L)).thenReturn(Arrays.asList(version1, version2));
        when(videoVersionRepository.findByVideoId(2L)).thenReturn(Arrays.asList(version1, version2));

        List<GetAllVideosResponse> result = videoService.getAllVideosByPostId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsOriginal());
        assertEquals(1, result.get(0).getVideoVersions().size());
    }

    @Test
    public void testGetAllOriginalVideosByPostId() {
        Video video1 = new Video();
        video1.setId(1L);
        Video video2 = new Video();
        video2.setId(2L);

        VideoVersion version1 = new VideoVersion();
        version1.setId(1L);
        version1.setVersionName("original");
        version1.setQuality("1080p");

        when(videoRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList(video1, video2));
        when(videoVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(version1);

        List<VersionResponse> result = videoService.getAllOriginalVideosByPostId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("original", result.get(0).getVersionName());
        assertEquals("1080p", result.get(0).getQuality());
    }

    @Test
    public void testGetVersionResponseById() throws IOException {
        Path testFile = tempDir.resolve("version-video.mp4");
        Files.createFile(testFile);

        VideoVersion videoVersion = new VideoVersion();
        videoVersion.setId(1L);
        videoVersion.setFilePath(testFile.toString());

        when(videoVersionRepository.findById(anyLong())).thenReturn(Optional.of(videoVersion));

        ResourceResponse result = videoService.getVersionResponseById(1L);

        assertNotNull(result);
        assertNotNull(result.getResource());
        assertTrue(result.getResource().exists());
    }

    @Test
    public void testUploadVideo_PostNotFound() {
        MockMultipartFile file = new MockMultipartFile("test.mp4", "test.mp4", "video/mp4", "test video content".getBytes());
        when(postService.getPostEntity(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, 1L, StorageType.FILE_SYSTEM, Arrays.asList("720p"));
        });
    }

    @Test
    public void testRemoveVideoFromPost_VideoNotFound() {
        when(videoRepository.findByIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            videoService.removeVideoFromPost(1L, 1L);
        });
    }

    @Test
    public void testGetOriginalVideoByVideoId_VideoNotFound() {
        when(videoVersionRepository.findOriginalVersionByVideoId(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            videoService.getOriginalVideoByVideoId(1L);
        });
    }

    @Test
    public void testGetAllVideosByPostId_NoVideos() {
        when(videoRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList());

        List<GetAllVideosResponse> result = videoService.getAllVideosByPostId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllOriginalVideosByPostId_NoVideos() {
        when(videoRepository.findAllByPostId(anyLong())).thenReturn(Arrays.asList());

        assertThrows(BusinessException.class, () -> {
            videoService.getAllOriginalVideosByPostId(1L);
        });
    }

    @Test
    public void testGetVersionResponseById_VersionNotFound() {
        when(videoVersionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            videoService.getVersionResponseById(1L);
        });
    }

    @Test
    public void testUploadVideo_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "test content".getBytes());
        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, 1L, StorageType.FILE_SYSTEM, Arrays.asList("720p"));
        });
    }
}