package com.blog.application.blog.services.image;

import com.blog.application.blog.dtos.common.ResourceResponse;
import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.dtos.responses.image.GetAllImagesResponse;
import com.blog.application.blog.dtos.responses.image.UploadedImageResponse;
import com.blog.application.blog.enums.StorageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    UploadedImageResponse uploadImage(MultipartFile file, Long postId, StorageType storageType, List<String> sizes) throws IOException;
    void removeImageFromPost(Long imageId, Long postId);
    ResourceResponse getOriginalImageByImageId(Long imageId);
    List<GetAllImagesResponse> getAllImagesByPostId(Long postId);
    List<VersionResponse> getAllOriginalImagesByPostId(Long postId);
    ResourceResponse getVersionResponseById(Long versionId);
}