package com.Auth.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class SessionNotFoundException  extends ApiException{
    public SessionNotFoundException() {
        super("Session not found" , HttpStatus.NOT_FOUND);
        log.warn("Session not found");
    }
}
