package com.blog.application.blog.dtos.requests.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Request object for user authentication")
public class AuthenticationRequest {

    @ApiModelProperty(value = "Username of the user attempting to authenticate", example = "john_doe")
    private String username;

    @ApiModelProperty(value = "Password of the user attempting to authenticate", example = "password123")
    private String password;
}
