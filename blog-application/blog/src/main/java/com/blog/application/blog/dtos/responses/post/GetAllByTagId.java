package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.responses.tag.TagResponse;
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
public class GetAllByTagId {
    private Long postId;
    private String title;
    private String text;
    private List<TagResponse> tags = new ArrayList<>();
}
