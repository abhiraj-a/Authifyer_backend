package com.Auth.Controller;

import com.Auth.DTO.SessionDTO;
import com.Auth.Entity.Session;
import com.Auth.Service.SessionService;
import com.Auth.Service.TokenService;
import com.Auth.Util.Extractor;
import com.Auth.Util.RefreshCookie;
import com.Auth.Util.RefreshResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("authifyer/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refresh_token")String token , HttpServletRequest request, HttpServletResponse response){

        String ip =Extractor.getClientIP(request);
        String device= Extractor.parseDeviceName(request);
        String user = request.getHeader("User-Agent");


        RefreshResult refreshResult = sessionService.rotateSession(token,ip,device,user);
        Session session =refreshResult.getSession();
        RefreshCookie.clear(response);
        RefreshCookie.set(response,refreshResult.getRawRefreshToken());

        return ResponseEntity.ok()
                .body(Map.of(
                        "access_token" , tokenService.issueAccessToken(refreshResult.getRawRefreshToken()),
                        "session", SessionDTO.builder()
                                .subjectId(session.getSubjectId())
                                .publicSessionId(session.getPublicId())
                                .publicProjectId(session.getPublicProjectId())
                                .createdAt(Instant.now())
                                .lastAccessedAt(Instant.now())
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refresh_token")String token ,HttpServletResponse response){
        sessionService.revokeSession(token);
        RefreshCookie.clear(response);
        return ResponseEntity.ok().build();
    }

}
