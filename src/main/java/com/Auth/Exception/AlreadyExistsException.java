package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException{
    public AlreadyExistsException() {
        super("Email already exists" , HttpStatus.BAD_REQUEST);
    }
}
