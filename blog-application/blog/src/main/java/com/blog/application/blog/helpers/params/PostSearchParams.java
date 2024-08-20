package com.blog.application.blog.helpers.params;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PostSearchParams {
    private Long id;
    private String title;
    private String text;
    private Long userId;
    private String tagName;
}
