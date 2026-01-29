package com.Auth.Service;
import com.Auth.DTO.*;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Project;
import com.Auth.Entity.Session;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Repo.SessionRepo;
import com.Auth.Util.IdGenerator;
import com.Auth.Util.RefreshResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalUserService {

    private final GlobalUserRepo globalUserRepo;

    private final PasswordEncoder passwordEncoder;

    private final SessionService sessionService;

    private final EmailService emailService;

    private final TokenService tokenService;

    private final ProjectRepo projectRepo;

    private final SessionRepo sessionRepo;

    public SessionDTO signup(RegisterRequest request,HttpServletRequest servletRequest, HttpServletResponse response) {

        GlobalUser user ;
        user = globalUserRepo.findByEmail(request.getEmail()).orElse(null);
        if(user!=null){
            throw new RuntimeException("User exist");
        }

        user= GlobalUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .subjectId(IdGenerator.generateGlobalUserSubjectId())
                .build();
        globalUserRepo.save(user);

        emailService.createVerificationToken(user);

        RefreshResult refreshResult =sessionService.createGlobalSession(user.getSubjectId()
                                   ,servletRequest,response);

        Session session=refreshResult.getSession();

         return SessionDTO.builder()
                 .publicProjectId(request.getPublicProjectId())
                 .subjectId(session.getSubjectId())
                 .publicSessionId(session.getPublicId())
                 .createdAt(Instant.now())
                 .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                 .lastAccessedAt(Instant.now())
                 .accessTokenClaims(tokenService.issueGlobalAccessToken(refreshResult.getRawRefreshToken()))
                 .build();
    }


    public SessionDTO login(LoginRequest request, HttpServletRequest servletRequest , HttpServletResponse response) {
        GlobalUser user = globalUserRepo.findByEmail(request.getEmail()).orElse(null);
        if(user==null){
            throw new RuntimeException("User doesnt exist");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account disabled.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        RefreshResult refreshResult =sessionService.createGlobalSession(user.getSubjectId(), servletRequest,response);
        Session session =refreshResult.getSession();
        return SessionDTO.builder()
                .subjectId(session.getSubjectId())
                .publicSessionId(session.getPublicId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .accessTokenClaims(tokenService.issueGlobalAccessToken(refreshResult.getRawRefreshToken()))
                .build();
    }

    public void softdelete(AuthPrincipal principal) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        user.setActive(false);
        globalUserRepo.save(user);
        List<Session> sessions = sessionRepo.findAllBySubjectIdAndRevokedAtIsNull(principal.getSubjectId());
        Instant now = Instant.now();

        for (Session session : sessions) {
            session.setRevokedAt(now);
        }

        sessionRepo.saveAll(sessions);
    }
}
