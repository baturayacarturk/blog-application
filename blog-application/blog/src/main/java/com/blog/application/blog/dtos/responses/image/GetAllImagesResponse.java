package com.blog.application.blog.dtos.responses.image;


import com.blog.application.blog.dtos.common.VersionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllImagesResponse {
    private Long id;
    private Boolean isOriginal;
    private List<VersionResponse> imageVersions;
}
