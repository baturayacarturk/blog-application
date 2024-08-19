package com.blog.application.blog.helpers.params.utils;

import org.springframework.util.StringUtils;

import java.util.List;

public class ExtendedStringUtils extends StringUtils {
    public static Boolean isNull(String input){
        return input == null || input.isEmpty()|| input.trim().isEmpty();
    }
    public static <T> Boolean listIsNullOrEmpty(List<T> object){
        return object == null || object.isEmpty();
    }
}
