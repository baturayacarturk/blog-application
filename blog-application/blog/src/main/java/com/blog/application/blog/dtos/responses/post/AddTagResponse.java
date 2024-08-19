package com.blog.application.blog.dtos.responses.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddTagResponse {
    private String name;
    private Long postId;
}
