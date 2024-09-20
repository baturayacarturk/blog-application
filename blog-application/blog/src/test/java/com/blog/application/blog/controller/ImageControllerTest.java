package com.blog.application.blog.controller;

import com.blog.application.blog.controllers.ImageController;
import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.image.GetAllImagesResponse;
import com.blog.application.blog.dtos.responses.image.UploadedImageResponse;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.services.image.ImageService;
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
import org.springframework.security.test.context.support.WithMockUser;
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
@WebMvcTest(controllers = ImageController.class)
@ComponentScan(basePackages = {"com.blog.application.blog.jwt"})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ImageController.class, ImageService.class})
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser")
    public void testUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        UploadedImageResponse response = new UploadedImageResponse();
        response.setId(1L);
        response.setStorageType(StorageType.FILE_SYSTEM);
        response.setImageVersions(Arrays.asList(new VersionResponse(1L, "original",null, 100, 100)));

        when(imageService.uploadImage(any(), anyLong(), any(), anyList())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(multipart("/api/images")
                .file(file)
                .param("postId", "1")
                .param("storageType", StorageType.FILE_SYSTEM.toString())
                .param("sizes", "100x100"));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.storageType").value("FILE_SYSTEM"))
                .andExpect(jsonPath("$.imageVersions[0].id").value(1))
                .andExpect(jsonPath("$.imageVersions[0].versionName").value("original"))
                .andExpect(jsonPath("$.imageVersions[0].width").value(100))
                .andExpect(jsonPath("$.imageVersions[0].height").value(100));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testRemoveImageFromPost() throws Exception {
        doNothing().when(imageService).removeImageFromPost(anyLong(), anyLong());

        ResultActions resultActions = mockMvc.perform(delete("/api/images/1")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetOriginalImageByImageId() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test image content".getBytes());
        ResourceResponse response = new ResourceResponse();
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.setResource(resource);

        when(imageService.getOriginalImageByImageId(anyLong())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/images/original")
                .param("imageId", "1")
                .accept(MediaType.IMAGE_JPEG));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("test image content".getBytes()));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetVersionResponseById() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test image content".getBytes());
        ResourceResponse response = new ResourceResponse();
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.setResource(resource);

        when(imageService.getVersionResponseById(anyLong())).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/api/images/version")
                .param("imageVersionId", "1")
                .accept(MediaType.IMAGE_JPEG));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("test image content".getBytes()));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetAllOriginalImagesByPostId() throws Exception {
        List<VersionResponse> responses = Arrays.asList(
                new VersionResponse(1L, "original", null,100, 100),
                new VersionResponse(2L, "original",null, 200, 200)
        );

        when(imageService.getAllOriginalImagesByPostId(anyLong())).thenReturn(responses);

        ResultActions resultActions = mockMvc.perform(get("/api/images/originals")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].versionName").value("original"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].versionName").value("original"));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetAllImagesByPostId() throws Exception {
        List<GetAllImagesResponse> responses = Arrays.asList(
                new GetAllImagesResponse(1L, true, Arrays.asList(new VersionResponse(1L, "original",null, 100, 100))),
                new GetAllImagesResponse(2L, true, Arrays.asList(new VersionResponse(2L, "original",null, 200, 200)))
        );

        when(imageService.getAllImagesByPostId(anyLong())).thenReturn(responses);

        ResultActions resultActions = mockMvc.perform(get("/api/images/findAll")
                .param("postId", "1")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].isOriginal").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].isOriginal").value(true));
    }

}