package com.Auth.Exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String message;
    private String error;
    private String path;
    private Instant timeStamp;
    private int status;
}
