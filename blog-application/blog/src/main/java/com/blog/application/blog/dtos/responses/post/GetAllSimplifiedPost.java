package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.common.SimplifiedPost;
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
@ApiModel(description = "Response containing a list of simplified posts")
public class GetAllSimplifiedPost {

    @ApiModelProperty(value = "List of simplified post details", example = "[{\"postId\":1, \"title\":\"Spring Boot Basics\", \"text\":\"Introduction to Spring Boot.\"}]")
    private List<SimplifiedPost> posts = new ArrayList<>();
}
