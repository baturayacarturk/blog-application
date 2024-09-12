package com.blog.application.blog.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElasticTagDto {

    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text)
    private String name;
}