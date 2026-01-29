package com.Auth.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/authifyer/login/oauth")
public class OAuthFailureController {
    @GetMapping("/failure")
    public ResponseEntity<?> failure(){
        return ResponseEntity.status(401).body(Map.of("error", "OAuth Login Failed or Cancelled"));
    }
}
