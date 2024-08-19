package com.blog.application.blog.helpers.params;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TagSearchParams {
    private Long id;
    private String name;
}
