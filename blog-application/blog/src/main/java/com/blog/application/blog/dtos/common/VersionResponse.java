package com.blog.application.blog.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionResponse {
    private Long id;
    private String versionName;
    @Nullable
    private String quality;
    @Nullable
    private Integer width;
    @Nullable
    private Integer height;
}
