package com.blog.application.blog.dtos.responses.video;

import com.blog.application.blog.dtos.common.VersionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllVideosResponse {
    private Long id;
    private Boolean isOriginal;
    private List<VersionResponse> videoVersions;
}
