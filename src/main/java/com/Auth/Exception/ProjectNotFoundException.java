package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends ApiException{
    public ProjectNotFoundException() {
        super("Project not found" , HttpStatus.NOT_FOUND);
    }
}
