package com.blog.application.blog.helpers.params.utils;

import org.springframework.util.StringUtils;

public class ExtendedStringUtils extends StringUtils {
    public static Boolean isNull(String input){
        return input == null || input.isEmpty()|| input.trim().isEmpty();
    }
}
