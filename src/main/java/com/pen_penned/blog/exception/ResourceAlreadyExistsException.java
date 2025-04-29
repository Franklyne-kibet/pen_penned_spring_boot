package com.pen_penned.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceAlreadyExistsException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}