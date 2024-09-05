package com.blog.application.blog.dtos.responses.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Response object for the deletion of a blog post")
public class DeletedPostResponse {

    @ApiModelProperty(value = "Message indicating the result of the delete operation", example = "Post with id: 123 is successfully deleted.")
    private String response;
}
