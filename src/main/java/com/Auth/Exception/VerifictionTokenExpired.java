package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class VerifictionTokenExpired  extends ApiException{
    public VerifictionTokenExpired() {
        super("Token Expired", HttpStatus.BAD_REQUEST);
    }
}
