package com.blog.application.blog.dtos.requests.post;

import com.blog.application.blog.dtos.common.TagDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

    private String title;

    private String text;

    private Long userId;

    private List<TagDto> tags = new ArrayList<>();
}
