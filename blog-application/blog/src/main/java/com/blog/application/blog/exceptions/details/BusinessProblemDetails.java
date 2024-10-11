package com.blog.application.blog.exceptions.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessProblemDetails {
    private int status;
    private String type;
    private String title;
    private String detail;
    private String instance;

}