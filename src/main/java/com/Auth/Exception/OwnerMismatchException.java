package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class OwnerMismatchException extends ApiException{
    public OwnerMismatchException() {
        super("Error : Owner mismatch" , HttpStatus.FORBIDDEN);
    }
}
