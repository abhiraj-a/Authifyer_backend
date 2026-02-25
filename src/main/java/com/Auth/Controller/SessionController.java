package com.Auth.Controller;

import com.Auth.DTO.SessionDTO;
import com.Auth.Entity.Session;
import com.Auth.Service.SessionService;
import com.Auth.Service.TokenService;
import com.Auth.Util.Extractor;
import com.Auth.Util.RefreshResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("authifyer/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> payload, HttpServletRequest request, HttpServletResponse response){

        String token = payload.get("refresh_token");
        String ip =Extractor.getClientIP(request);
        String device= Extractor.parseDeviceName(request);
        String user = request.getHeader("User-Agent");


        RefreshResult refreshResult = sessionService.rotateSession(token,ip,device,user);
        Session session =refreshResult.getSession();
//        RefreshCookie.clear(response);
//        RefreshCookie.set(response,refreshResult.getRawRefreshToken());

        return ResponseEntity.ok()
                .body(Map.of(
                        "access_token" , tokenService.issueAccessToken(refreshResult.getRawRefreshToken()),
                        "refresh_token",refreshResult.getRawRefreshToken(),
                        "session", SessionDTO.builder()
                                .subjectId(session.getSubjectId())
                                .publicSessionId(session.getPublicId())
                                .publicProjectId(session.getPublicProjectId())
                                .createdAt(Instant.now())
                                .lastAccessedAt(Instant.now())
                                .refreshToken(refreshResult.getRawRefreshToken())
                                .build()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String,String> payload ,HttpServletResponse response){

        String token = payload.get("refresh_token");
        if(token!=null&&!token.isBlank()) {
            sessionService.revokeSession(token);
        }
//        RefreshCookie.clear(response);
        return ResponseEntity.ok().build();
    }


}
