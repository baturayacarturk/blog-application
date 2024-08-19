package com.blog.application.blog.dtos.requests.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    private Long id;
    private String title;
    private String text;
}
