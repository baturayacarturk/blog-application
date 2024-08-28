package com.blog.application.blog.dtos.responses.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Response containing details of a tag")
public class TagResponse {

    @ApiModelProperty(value = "Unique identifier of the tag", example = "1")
    private Long id;

    @ApiModelProperty(value = "Name of the tag", example = "Technology")
    private String name;
}
