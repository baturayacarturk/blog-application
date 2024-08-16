package com.blog.application.blog.dtos.requests.post;

import com.blog.application.blog.dtos.common.TagDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

    private String title;

    private String text;

    private Long userId;

    private Set<TagDto> tags = new HashSet<>();
}
