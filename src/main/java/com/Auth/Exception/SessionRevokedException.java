package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class SessionRevokedException  extends  ApiException{
    public SessionRevokedException() {
        super("Session revoked" , HttpStatus.SERVICE_UNAVAILABLE);
    }
}
