package com.blog.application.blog.dtos.requests.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Request object for user registration")
public class RegisterRequest {

    @ApiModelProperty(value = "Username chosen by the user for registration", example = "john_doe")
    private String username;

    @ApiModelProperty(value = "Display name of the user", example = "John Doe")
    private String displayName;

    @ApiModelProperty(value = "Password chosen by the user for registration", example = "securePassword123")
    private String password;
}
