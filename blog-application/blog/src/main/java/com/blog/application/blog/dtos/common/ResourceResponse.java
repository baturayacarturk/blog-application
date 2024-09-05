package com.blog.application.blog.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private String contentType;
    private Resource resource;
}