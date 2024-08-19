package com.blog.application.blog.exceptions.types;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException{
    private List<String> errors;

    public ValidationException(String message, ArrayList<String> errors){
        super(message);
        this.errors=errors;
    }
    public ValidationException(String message){
        super(message);
    }
    public List<String> getErrors() {
        return errors;
    }
}