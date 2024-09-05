package com.blog.application.blog.dtos.responses.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Response object for adding a tag to a post")
public class AddTagResponse {

    @ApiModelProperty(value = "Name of the tag that was added", example = "Tech")
    private String name;

    @ApiModelProperty(value = "ID of the post to which the tag was added", example = "101")
    private Long postId;
}
