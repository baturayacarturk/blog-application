package com.blog.application.blog.services.image;

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
import com.blog.application.blog.helpers.params.utils.SecurityUtils;
import com.blog.application.blog.repositories.ImageRepository;
import com.blog.application.blog.repositories.ImageVersionRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.storage.FileStorageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final ImageVersionRepository imageVersionRepository;
    private final FileStorageService fileStorageService;
    private final PostService postService;
    private final static String ORIGINAL = "original";

    public ImageServiceImpl(ImageRepository imageRepository, ImageVersionRepository imageVersionRepository,
                            FileStorageService fileStorageService, PostService postService) {
        this.imageRepository = imageRepository;
        this.imageVersionRepository = imageVersionRepository;
        this.fileStorageService = fileStorageService;
        this.postService = postService;
    }

    @Transactional
    @Override
    public UploadedImageResponse uploadImage(MultipartFile file, Long postId, StorageType storageType, List<String> sizes) throws IOException {
        User currentUser = SecurityUtils.extractUserFromSecurityContext();
        Post post = postService.getPostEntity(postId);

        if (post == null) {
            throw new BusinessException(String.format("Post with id: %d could not be found", postId));
        }

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You are not authorized to upload images to this post");
        }

        BufferedImage bufferedImage;
        try (InputStream inputStream = file.getInputStream()) {
            bufferedImage = ImageIO.read(inputStream);
        }
        int originalWidth = bufferedImage.getWidth();
        int originalHeight = bufferedImage.getHeight();

        Image image = new Image();
        image.setFileName(file.getOriginalFilename());
        image.setStorageType(storageType);
        image.setPost(post);

        Image savedImaged = imageRepository.save(image);

        List<VersionResponse> imageVersionResponses = new ArrayList<>();
        createVersionResponse(image, file, originalWidth, originalHeight, ORIGINAL, true, imageVersionResponses);

        if (sizes != null && !sizes.isEmpty()) {
            for (String size : sizes) {
                String[] dimensions = size.split("x");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                createVersionResponse(image, file, width, height, size, false, imageVersionResponses);
            }
        }
        UploadedImageResponse response = new UploadedImageResponse();
        response.setId(savedImaged.getId());
        response.setStorageType(image.getStorageType());
        response.setFileName(image.getFileName());
        response.setImageVersions(imageVersionResponses);

        return response;
    }

    @Override
    public void removeImageFromPost(Long imageId, Long postId) {
        User currentUser = SecurityUtils.extractUserFromSecurityContext();
        Image image = imageRepository.findByIdAndPostId(imageId, postId)
                .orElseThrow(() -> new BusinessException("Image not found"));

        if (!image.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You are not authorized to remove this image");
        }

        for (ImageVersion version : image.getVersions()) {
            if (image.getStorageType() == StorageType.FILE_SYSTEM) {
                fileStorageService.deleteFile(version.getFilePath());
            }
            imageVersionRepository.delete(version);
        }

        imageRepository.delete(image);
    }

    @Override
    public ResourceResponse getOriginalImageByImageId(Long imageId) {
        ImageVersion image = imageVersionRepository.findOriginalVersionByVideoId(imageId);
        if (image == null) {
            throw new BusinessException("Image not found");
        }
        ResourceResponse response = new ResourceResponse();
        try {
            Path filePath = Paths.get(image.getFilePath());
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

    @Override
    public List<GetAllImagesResponse> getAllImagesByPostId(Long postId) {
        List<Image> images = imageRepository.findAllByPostId(postId);

        if (images != null) {
            return images.stream().map(image -> {
                List<ImageVersion> imageVersions = imageVersionRepository.findByImageId(image.getId());
                ImageVersion originalVersionResponse = imageVersions.stream()
                        .filter(ImageVersion::isOriginal)
                        .findFirst()
                        .orElse(null);
                GetAllImagesResponse getAllImagesResponse = new GetAllImagesResponse();
                getAllImagesResponse.setIsOriginal(originalVersionResponse.isOriginal());
                getAllImagesResponse.setId(originalVersionResponse.getId());
                List<VersionResponse> nonOriginalsVersionResponsesResponses = imageVersions.stream()
                        .filter(imageVersion -> !imageVersion.isOriginal())
                        .map(this::convertToVersionResponseResponse)
                        .toList();

                getAllImagesResponse.setImageVersions(nonOriginalsVersionResponsesResponses);
                return getAllImagesResponse;

            }).collect(Collectors.toList());

        }
        return null;
    }

    @Override
    public List<VersionResponse> getAllOriginalImagesByPostId(Long postId) {
        List<Image> images = imageRepository.findAllByPostId(postId);
        if (CollectionUtils.isEmpty(images)) {
            throw new BusinessException(String.format("Images of post with id {} could not be found", postId));
        }

        return images.stream().map(image -> {
            ImageVersion imageVersion = imageVersionRepository.findOriginalVersionByVideoId(image.getId());
            VersionResponse imageVersionResponse = new VersionResponse();
            imageVersionResponse.setVersionName(imageVersion.getVersionName());
            imageVersionResponse.setId(imageVersion.getId());
            imageVersionResponse.setHeight(imageVersion.getHeight());
            imageVersionResponse.setWidth(imageVersion.getWidth());
            return imageVersionResponse;
        }).toList();
    }

    @Override
    public ResourceResponse getVersionResponseById(Long imageVersionId) {
        ImageVersion imageVersion = imageVersionRepository.findById(imageVersionId)
                .orElseThrow(() -> new BusinessException("Image version not found"));
        ResourceResponse response = new ResourceResponse();
        try {
            Path filePath = Paths.get(imageVersion.getFilePath());
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

    private void createVersionResponse(Image image, MultipartFile file, int width, int height, String versionName, boolean isOriginal, List<VersionResponse> imageVersionResponses) throws IOException {
        ImageVersion imageVersion = new ImageVersion();
        imageVersion.setImage(image);
        imageVersion.setVersionName(versionName);
        imageVersion.setWidth(width);
        imageVersion.setHeight(height);
        imageVersion.setOriginal(isOriginal);

        if (image.getStorageType() == StorageType.FILE_SYSTEM) {
            String filePath = fileStorageService.resizeAndSaveImage(file, width, height, versionName);
            imageVersion.setFilePath(filePath);
        } else {
            // Handle azure blob storage etc. maybe apply strategy
            imageVersion.setFilePath(null);
        }

        ImageVersion versionedImage = imageVersionRepository.save(imageVersion);
        VersionResponse imageVersionResponse = new VersionResponse(imageVersion.getId(), versionedImage.getVersionName(),null, imageVersion.getWidth(), imageVersion.getHeight());
        imageVersionResponses.add(imageVersionResponse);
        image.getVersions().add(versionedImage);
    }

    private VersionResponse convertToVersionResponseResponse(ImageVersion imageVersion) {
        return new VersionResponse(
                imageVersion.getId(),
                imageVersion.getVersionName(),
                null,
                imageVersion.getWidth(),
                imageVersion.getHeight()
        );
    }
}