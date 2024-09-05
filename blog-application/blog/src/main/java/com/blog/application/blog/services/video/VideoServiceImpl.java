package com.blog.application.blog.services.video;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.video.GetAllVideosResponse;
import com.blog.application.blog.dtos.responses.video.UploadedVideoResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.entities.Video;
import com.blog.application.blog.entities.VideoVersion;
import com.blog.application.blog.enums.StorageType;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.helpers.params.utils.SecurityUtils;
import com.blog.application.blog.repositories.VideoRepository;
import com.blog.application.blog.repositories.VideoVersionRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.storage.FileStorageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final FileStorageService fileStorageService;
    private final PostService postService;

    public VideoServiceImpl(VideoRepository videoRepository, VideoVersionRepository videoVersionRepository, FileStorageService fileStorageService, PostService postService) {
        this.videoRepository = videoRepository;
        this.videoVersionRepository = videoVersionRepository;
        this.fileStorageService = fileStorageService;
        this.postService = postService;
    }

    @Transactional
    public UploadedVideoResponse uploadVideo(MultipartFile file, Long postId, StorageType storageType, List<String> qualities) throws IOException {
        User currentUser = SecurityUtils.extractUserFromSecurityContext();
        Post post = postService.getPostEntity(postId);
        if (post == null) {
            throw new BusinessException(String.format("Post with id: %d could not be found", postId));
        }
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You are not authorized to upload images to this post");
        }
        boolean isVideo = isVideoFile(file);
        if (!isVideo) {
            throw new BusinessException("This file is not a valid video type");
        }

        Video video = new Video();
        video.setFileName(file.getOriginalFilename());
        video.setStorageType(storageType);
        video.setPost(postService.getPostEntity(postId));
        List<VersionResponse> videoVersionResponses = new ArrayList<>();
        videoRepository.save(video);
        createVersionResponse(video, file, "original", "original", true, videoVersionResponses);

        if (qualities != null && !qualities.isEmpty()) {
            for (String quality : qualities) {
                createVersionResponse(video, file, quality, quality, false, videoVersionResponses);
            }
        }

        UploadedVideoResponse response = new UploadedVideoResponse();
        response.setId(video.getId());
        response.setFileName(video.getFileName());
        response.setStorageType(video.getStorageType());
        response.setVideoVersions(videoVersionResponses);

        return response;
    }

    public void removeVideoFromPost(Long videoId, Long postId) {
        Video video = videoRepository.findByIdAndPostId(videoId, postId)
                .orElseThrow(() -> new BusinessException("Video not found"));

        for (VideoVersion version : video.getVersions()) {
            if (video.getStorageType() == StorageType.FILE_SYSTEM) {
                fileStorageService.deleteFile(version.getFilePath());
            }
            videoVersionRepository.delete(version);
        }

        videoRepository.delete(video);
    }

    public ResourceResponse getOriginalVideoByVideoId(Long videoId) {
        VideoVersion video = videoVersionRepository.findOriginalVersionByVideoId(videoId);
        if (video == null) {
            throw new BusinessException("Video not found");
        }
        ResourceResponse response = new ResourceResponse();
        try {
            Path filePath = Paths.get(video.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                response.setResource(resource);
                response.setContentType(Files.probeContentType(filePath));
            } else {
                throw new BusinessException("File is not readable or does not exist");
            }
        } catch (Exception e) {
            throw new BusinessException("Could not read file: " + e.getMessage());
        }


        return response;
    }

    public ResourceResponse getVersionResponseById(Long videoVersionId) {
        VideoVersion videoVersion = videoVersionRepository.findById(videoVersionId)
                .orElseThrow(() -> new BusinessException("Video version not found"));
        ResourceResponse response = new ResourceResponse();
        try {
            Path filePath = Paths.get(videoVersion.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                response.setResource(resource);
                response.setContentType(Files.probeContentType(filePath));
            } else {
                throw new BusinessException("File is not readable or does not exist");
            }
        } catch (Exception e) {
            throw new BusinessException("Could not read file: " + e.getMessage());
        }
        return response;
    }


    public List<GetAllVideosResponse> getAllVideosByPostId(Long postId) {
        List<Video> videos = videoRepository.findAllByPostId(postId);

        if (videos != null) {
            return videos.stream().map(video -> {
                List<VideoVersion> videoVersions = videoVersionRepository.findByVideoId(video.getId());
                VideoVersion originalVersionResponse = videoVersions.stream()
                        .filter(VideoVersion::isOriginal)
                        .findFirst()
                        .orElse(null);
                GetAllVideosResponse getAllVideosResponse = new GetAllVideosResponse();
                getAllVideosResponse.setIsOriginal(originalVersionResponse.isOriginal());
                getAllVideosResponse.setId(originalVersionResponse.getId());
                List<VersionResponse> nonOriginalsVersionResponsesResponses = videoVersions.stream()
                        .filter(videoVersion -> !videoVersion.isOriginal())
                        .map(this::convertToVersionResponseResponse)
                        .toList();

                getAllVideosResponse.setVideoVersions(nonOriginalsVersionResponsesResponses);
                return getAllVideosResponse;

            }).collect(Collectors.toList());

        }
        return null;
    }

    public List<com.blog.application.blog.dtos.common.VersionResponse> getAllOriginalVideosByPostId(Long postId) {
        List<Video> videos = videoRepository.findAllByPostId(postId);

        if (CollectionUtils.isEmpty(videos)) {
            throw new BusinessException(String.format("Videos of post with id {} could not be found", postId));
        }

        return videos.stream().map(video -> {
            VideoVersion videoVersion = videoVersionRepository.findOriginalVersionByVideoId(video.getId());
            VersionResponse videoVersionResponse = new VersionResponse();
            videoVersionResponse.setVersionName(videoVersion.getVersionName());
            videoVersionResponse.setId(videoVersion.getId());
            videoVersionResponse.setQuality(videoVersion.getQuality());
            return videoVersionResponse;
        }).toList();
    }

    private void createVersionResponse(Video video, MultipartFile file, String versionName, String quality, boolean isOriginal, List<VersionResponse> videoVersionResponses) throws IOException {
        VideoVersion videoVersion = new VideoVersion();
        videoVersion.setVideo(video);
        videoVersion.setVersionName(versionName);
        videoVersion.setQuality(quality);
        videoVersion.setOriginal(isOriginal);

        if (video.getStorageType() == StorageType.FILE_SYSTEM) {
            String filePath = fileStorageService.saveVideoWithQuality(file, quality);
            videoVersion.setFilePath(filePath);
        } else {
            videoVersion.setFilePath(null);
        }

        VideoVersion videoVersionEntitiy = videoVersionRepository.save(videoVersion);
        VersionResponse versionResponse = new VersionResponse(videoVersionEntitiy.getId(), videoVersionEntitiy.getVersionName(), videoVersionEntitiy.getQuality(), null, null);
        videoVersionResponses.add(versionResponse);
        video.getVersions().add(videoVersion);
    }

    private boolean isVideoFile(MultipartFile file) {
        String mimeType = file.getContentType();
        return mimeType != null && mimeType.startsWith("video/");
    }

    private VersionResponse convertToVersionResponseResponse(VideoVersion videoVersion) {
        return new VersionResponse(
                videoVersion.getId(),
                videoVersion.getVersionName(),
                videoVersion.getQuality(),
                null, null
        );
    }


}