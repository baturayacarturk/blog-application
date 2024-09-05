package com.blog.application.blog.entities.elastic;

import com.blog.application.blog.dtos.common.ElasticTagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "elastic_posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElasticPost {

    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String text;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<ElasticTagDto> tags;
}
