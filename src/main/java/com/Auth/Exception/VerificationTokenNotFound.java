package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class VerificationTokenNotFound extends ApiException{
    public VerificationTokenNotFound() {
        super("Invalid Token", HttpStatus.NOT_FOUND);
    }
}
