package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentailsException extends ApiException{
    public InvalidCredentailsException( ) {
        super("Invalid credentials", HttpStatus.FORBIDDEN);
    }
}
