package com.Auth.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e , HttpServletRequest request){
        Error error = Error.builder()
                .error(e.getHttpStatus().getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timeStamp(Instant.now())
                .status(e.getHttpStatus().value())
                .build();
        return ResponseEntity.status(error.getStatus()).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnhandled(Exception e , HttpServletRequest request){
        Error errorResponse = Error.builder()
                .error("Internal Server Error")
                .status(500)
                .timeStamp(Instant.now())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError()
                .body(errorResponse);
    }
}
