package com.blog.application.blog.dtos.responses.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Response containing details of the updated post")
public class UpdatedPostResponse {

    @ApiModelProperty(value = "ID of the updated post", example = "1")
    private Long postId;

    @ApiModelProperty(value = "Title of the updated post", example = "Updated Spring Boot Basics")
    private String title;

    @ApiModelProperty(value = "Text content of the updated post", example = "This post provides an updated introduction to Spring Boot.")
    private String text;
}
