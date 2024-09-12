package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.video.GetAllVideosResponse;
import com.blog.application.blog.dtos.responses.video.UploadedVideoResponse;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.services.video.VideoService;
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
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    public ResponseEntity<UploadedVideoResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("postId") Long postId,
            @RequestParam("storageType") StorageType storageType,
            @RequestParam(value = "qualities", required = false) List<String> qualities) throws IOException {
        UploadedVideoResponse response = videoService.uploadVideo(file, postId, storageType, qualities);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> removeVideosFromPost(
            @PathVariable Long videoId,
            @RequestParam("postId") Long postId) {
        videoService.removeVideoFromPost(videoId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/original")
    public ResponseEntity<Resource> getOriginalVideoById(@RequestParam Long videoId) {
        ResourceResponse videoResponse = videoService.getOriginalVideoByVideoId(videoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(videoResponse.getContentType()))
                .body(videoResponse.getResource());
    }

    @GetMapping("/version")
    public ResponseEntity<Resource> getVersionResponseById(@RequestParam Long videoVersionId) {
        ResourceResponse videoResponse = videoService.getVersionResponseById(videoVersionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(videoResponse.getContentType()))
                .body(videoResponse.getResource());
    }

    @GetMapping("/originals")
    public ResponseEntity<List<VersionResponse>> getAllOriginalVideosByPostId(@RequestParam Long postId) {
        List<VersionResponse> videos = videoService.getAllOriginalVideosByPostId(postId);
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @GetMapping("findAll")
    public ResponseEntity<List<GetAllVideosResponse>> getAllVideosByPostId(@RequestParam Long postId) {
        List<GetAllVideosResponse> response = videoService.getAllVideosByPostId(postId);
        if (CollectionUtils.isEmpty(response)) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
