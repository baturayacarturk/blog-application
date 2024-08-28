package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.responses.tag.TagResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Details of a blog post associated with a specific tag")
public class GetAllByTagId {

    @ApiModelProperty(value = "Unique identifier of the post", example = "1")
    private Long postId;

    @ApiModelProperty(value = "Title of the post", example = "Spring Boot and Swagger")
    private String title;

    @ApiModelProperty(value = "Content of the post", example = "This post explains how to integrate Swagger with Spring Boot.")
    private String text;

    @ApiModelProperty(value = "List of tags associated with the post")
    private List<TagResponse> tags = new ArrayList<>();
}
