package com.blog.application.blog.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private Long postId;
    private String title;
    private String text;
    private Set<TagDto> tags = new HashSet<>();
}
