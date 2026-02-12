package com.Auth.Controller;

import com.Auth.Service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/api/v1/server")
@RequiredArgsConstructor
public class ServerApiController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<?> toggleStatusViaKey(@RequestHeader("x-secret-key")String secretKey
            , @RequestBody Map<String, String> payload , HttpServletResponse response){
        String authyfierId = payload.get("authyfierId");
        if(authyfierId==null||authyfierId.isBlank()){
            return ResponseEntity.badRequest().build();
        }
        try {
            projectService.toggleStatusViaKey(secretKey,payload);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        return ResponseEntity.ok().build();

    }
}
