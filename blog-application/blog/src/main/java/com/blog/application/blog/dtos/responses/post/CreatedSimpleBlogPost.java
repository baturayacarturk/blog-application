package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.common.TagDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatedSimpleBlogPost {
    private String title;

    private String text;

    //TODO remove when jwt added.
    private Long userId;

    private Set<TagDto> tags = new HashSet<>();
}
