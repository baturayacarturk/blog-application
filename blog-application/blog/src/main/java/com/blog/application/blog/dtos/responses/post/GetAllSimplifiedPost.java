package com.blog.application.blog.dtos.responses.post;

import com.blog.application.blog.dtos.common.SimplifiedPost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllSimplifiedPost {
    List<SimplifiedPost> posts = new ArrayList<>();
}
