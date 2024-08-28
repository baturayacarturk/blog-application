package com.blog.application.blog.dtos.requests.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing blog post.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Request payload for updating an existing blog post.")
public class UpdatePostRequest {

    @ApiModelProperty(value = "ID of the blog post to be updated", example = "1")
    private Long id;

    @ApiModelProperty(value = "New title of the blog post", example = "Updated Title")
    private String title;

    @ApiModelProperty(value = "New content of the blog post", example = "This is the updated content of the blog post.")
    private String text;
}
