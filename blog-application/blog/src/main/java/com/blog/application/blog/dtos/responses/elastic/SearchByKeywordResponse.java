package com.blog.application.blog.dtos.responses.elastic;

import com.blog.application.blog.dtos.common.ElasticTagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchByKeywordResponse {
    private Long id;

    private String title;

    private String text;

    private Long userId;

    private List<ElasticTagDto> tags;

}
