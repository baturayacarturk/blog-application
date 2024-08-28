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
@ApiModel(description = "Response object for the creation of a simple blog post")
public class CreatedSimpleBlogPost {

    @ApiModelProperty(value = "ID of the created post", example = "123")
    private Long postId;

    @ApiModelProperty(value = "Title of the created post", example = "My First Blog Post")
    private String title;

    @ApiModelProperty(value = "Content of the created post", example = "This is the content of my first blog post.")
    private String text;

    @ApiModelProperty(value = "List of tags associated with the created post")
    private List<TagResponse> tags = new ArrayList<>();
}
