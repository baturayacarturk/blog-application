package com.blog.application.blog.controller;

import com.blog.application.blog.controllers.VideoController;
import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.video.GetAllVideosResponse;
import com.blog.application.blog.dtos.responses.video.UploadedVideoResponse;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.services.video.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = VideoController.class)
@ComponentScan(basePackages = {"com.blog.application.blog.jwt"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {VideoController.class, VideoService.class})
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUploadVideo() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test video content".getBytes());

        UploadedVideoResponse response = new UploadedVideoResponse();
        response.setId(1L);
        response.setFileName("test.mp4");
        response.setStorageType(StorageType.FILE_SYSTEM);
        response.setVideoVersions(Arrays.asList(new VersionResponse(1L, "original", "720p",null,null)));

        when(videoService.uploadVideo(any(), anyLong(), any(), anyList())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(multipart("/api/videos")
                .file(file)
                .param("postId", "1")
                .param("storageType", StorageType.FILE_SYSTEM.toString())
                .param("qualities", "720p"));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test.mp4"))
                .andExpect(jsonPath("$.storageType").value("FILE_SYSTEM"))
                .andExpect(jsonPath("$.videoVersions[0].id").value(1))
                .andExpect(jsonPath("$.videoVersions[0].versionName").value("original"))
                .andExpect(jsonPath("$.videoVersions[0].quality").value("720p"));
    }

    @Test
    public void testRemoveVideoFromPost() throws Exception {
        doNothing().when(videoService).removeVideoFromPost(anyLong(), anyLong());

        ResultActions resultActions = mockMvc.perform(delete("/api/videos/1")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testGetOriginalVideoById() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test video content".getBytes());
        ResourceResponse response = new ResourceResponse();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setResource(resource);

        when(videoService.getOriginalVideoByVideoId(anyLong())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/videos/original")
                .param("videoId", "1")
                .accept(MediaType.APPLICATION_OCTET_STREAM));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes("test video content".getBytes()));
    }

    @Test
    public void testGetVersionResponseById() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test video content".getBytes());
        ResourceResponse response = new ResourceResponse();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setResource(resource);

        when(videoService.getVersionResponseById(anyLong())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/videos/version")
                .param("videoVersionId", "1")
                .accept(MediaType.APPLICATION_OCTET_STREAM));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes("test video content".getBytes()));
    }

    @Test
    public void testGetAllOriginalVideosByPostId() throws Exception {
        List<VersionResponse> responses = Arrays.asList(
                new VersionResponse(1L, "original", "720p",null,null),
                new VersionResponse(2L, "original", "1080p",null,null)
        );

        when(videoService.getAllOriginalVideosByPostId(anyLong())).thenReturn(responses);

        ResultActions resultActions = mockMvc.perform(get("/api/videos/originals")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].versionName").value("original"))
                .andExpect(jsonPath("$[0].quality").value("720p"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].versionName").value("original"))
                .andExpect(jsonPath("$[1].quality").value("1080p"));
    }

    @Test
    public void testGetAllVideosByPostId() throws Exception {
        List<GetAllVideosResponse> responses = Arrays.asList(
                new GetAllVideosResponse(1L, true, Arrays.asList(new VersionResponse(1L, "original", "720p",null,null))),
                new GetAllVideosResponse(2L, true, Arrays.asList(new VersionResponse(2L, "original", "1080p",null,null)))
        );

        when(videoService.getAllVideosByPostId(anyLong())).thenReturn(responses);

        ResultActions resultActions = mockMvc.perform(get("/api/videos/findAll")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].isOriginal").value(true))
                .andExpect(jsonPath("$[0].videoVersions[0].quality").value("720p"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].isOriginal").value(true))
                .andExpect(jsonPath("$[1].videoVersions[0].quality").value("1080p"));
    }

    @Test
    public void testGetAllVideosByPostId_NoContent() throws Exception {
        when(videoService.getAllVideosByPostId(anyLong())).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(get("/api/videos/findAll")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
    }
}