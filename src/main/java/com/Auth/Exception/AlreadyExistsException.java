package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException{
    public AlreadyExistsException() {
                super("User already exists by oauth . Please continue with oauth login" , HttpStatus.BAD_REQUEST);
    }
}
