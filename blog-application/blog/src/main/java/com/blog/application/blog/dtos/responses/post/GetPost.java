package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.common.PostDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPost {
    List<PostDto> posts = new ArrayList<>();

}
