package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class SessionNotFoundException  extends ApiException{
    public SessionNotFoundException() {
        super("Session not found" , HttpStatus.NOT_FOUND);
    }
}
