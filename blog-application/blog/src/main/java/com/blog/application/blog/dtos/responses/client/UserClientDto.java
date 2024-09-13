package com.blog.application.blog.dtos.responses.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClientDto {
    private Long id;
    private String username;

}