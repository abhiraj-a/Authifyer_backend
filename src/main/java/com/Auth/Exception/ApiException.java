package com.Auth.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    public ApiException(String mss , HttpStatus httpStatus){
        super(mss);
        this.httpStatus=httpStatus;
    }
}
