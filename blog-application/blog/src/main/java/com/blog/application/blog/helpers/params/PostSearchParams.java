package com.blog.application.blog.helpers.params;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Builder
@Getter
@Setter
public class PostSearchParams {
    private String title;
    private String text;
    private Long userId;
    private Set<String> tagNames;
}
