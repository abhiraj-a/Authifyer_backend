package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException  extends ApiException{

    public UnauthorizedException() {
        super("Unauthorized", HttpStatus.UNAUTHORIZED);
    }
}
