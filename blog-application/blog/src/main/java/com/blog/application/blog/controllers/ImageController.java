package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.image.GetAllImagesResponse;
import com.blog.application.blog.dtos.responses.image.UploadedImageResponse;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.services.image.ImageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<UploadedImageResponse> uploadImage(@RequestParam("file") MultipartFile file,
                                                             @RequestParam("postId") Long postId,
                                                             @RequestParam("storageType") StorageType storageType,
                                                             @RequestParam("sizes") List<String> sizes) throws IOException {
        UploadedImageResponse UploadedImageResponse = imageService.uploadImage(file, postId, storageType, sizes);
        return new ResponseEntity<>(UploadedImageResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> removeImageFromPost(@PathVariable Long imageId, @RequestParam Long postId) {
        imageService.removeImageFromPost(imageId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/original")
    public ResponseEntity<Resource> getOriginalImageByImageId(@RequestParam Long imageId) {
        ResourceResponse response = imageService.getOriginalImageByImageId(imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .body(response.getResource());
    }

    @GetMapping("version")
    public ResponseEntity<Resource> getVersionResponseById(@RequestParam Long imageVersionId) {
        ResourceResponse response = imageService.getVersionResponseById(imageVersionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .body(response.getResource());
    }

    @GetMapping("/originals")
    public ResponseEntity<List<VersionResponse>> getAllOriginalImagesByPostId(@RequestParam Long postId) {
        List<VersionResponse> response = imageService.getAllOriginalImagesByPostId(postId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/findAll")
    public ResponseEntity<List<GetAllImagesResponse>> getVersionResponse(@RequestParam Long postId) {
        List<GetAllImagesResponse> response = imageService.getAllImagesByPostId(postId);
        if (CollectionUtils.isEmpty(response)) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}