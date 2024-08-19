package com.blog.application.blog.exceptions.details;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ValidationProblemDetails {
    private int status;
    private String type;
    private String title;
    private List<String> detail;
    private String instance;

}