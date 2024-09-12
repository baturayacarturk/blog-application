package com.blog.application.blog.dtos.responses.image;

import com.blog.application.blog.dtos.common.VersionResponse;
import com.blog.application.blog.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedImageResponse {

    private Long id;
    private String fileName;
    private StorageType storageType;
    private List<VersionResponse> imageVersions;

}