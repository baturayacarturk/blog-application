package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.common.TagDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatedSimpleBlogPost {
    private String title;

    private String text;

    //TODO remove when jwt added.
    private Long userId;

    private List<TagDto> tags = new ArrayList<>();
}
