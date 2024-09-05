package com.blog.application.blog.services.video;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.video.GetAllVideosResponse;
import com.blog.application.blog.dtos.responses.video.UploadedVideoResponse;
import com.blog.application.blog.enums.StorageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    UploadedVideoResponse uploadVideo(MultipartFile file, Long postId, StorageType storageType, List<String> qualities) throws IOException;
    void removeVideoFromPost(Long videoId, Long postId);
    List<GetAllVideosResponse> getAllVideosByPostId(Long postId);
    List<VersionResponse> getAllOriginalVideosByPostId(Long postId);
    ResourceResponse getOriginalVideoByVideoId(Long videoId);
    ResourceResponse getVersionResponseById(Long videoVersionId) ;
}
