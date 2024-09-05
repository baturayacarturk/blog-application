package com.blog.application.blog.dtos.requests.post;

import com.blog.application.blog.dtos.common.TagDto;
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
@ApiModel(description = "Request payload for creating a new blog post.")
public class CreatePostRequest {

    @ApiModelProperty(value = "Title of the blog post", example = "Spring Boot Tips")
    private String title;

    @ApiModelProperty(value = "Content of the blog post", example = "This post contains tips and tricks for using Spring Boot effectively.")
    private String text;

    @ApiModelProperty(value = "ID of the user who is creating the post", example = "1")
    private Long userId;

    @ApiModelProperty(value = "List of tags associated with the blog post")
    private List<TagDto> tags = new ArrayList<>();
}
