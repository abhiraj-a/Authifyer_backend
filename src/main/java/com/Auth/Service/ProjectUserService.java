package com.Auth.Service;
import com.Auth.DTO.*;
import com.Auth.Entity.*;
import com.Auth.Exception.AccountSuspendedEXception;
import com.Auth.Exception.AlreadyExistsException;
import com.Auth.Exception.InvalidCredentailsException;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.*;
import com.Auth.Util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserService {


    private final SessionService sessionService;
    private final ProjectUserRepo projectUserRepo;
    private final TokenService tokenService;
    private final ProjectRepo projectRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SessionRepo sessionRepo;
    private final OAuthStorageRepo oAuthStorageRepo;

    @Transactional
    public SessionDTO signup_email_password(PasswordProjectRegisterRequest request , HttpServletRequest servletRequest,
                                            HttpServletResponse response) {

        Project project = projectRepo.findByPublicProjectId(request.getPublicProjectId()).orElseThrow(RuntimeException::new);

        if(projectUserRepo.existsByProjectAndEmail(project,request.getEmail())) {
            throw new AlreadyExistsException();
        }

        ProjectUser projectUser = projectUserRepo.save(ProjectUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .project(project)
                .authifyerId(IdGenerator.generateAuthifyerId())
                .createdAt(Instant.now())
                .build());

        emailService.createVerificationToken(projectUser);


        RefreshResult refreshResult = sessionService.createSession(projectUser.getAuthifyerId(),  project.getPublicProjectId(),servletRequest,response );

        Session session =refreshResult.getSession();
        project.getProjectUsers().add(projectUser);
        projectRepo.save(project);

        AccessTokenClaims claims =tokenService.issueAccessToken(refreshResult.getRawRefreshToken());
        return SessionDTO.builder()
                .subjectId(projectUser.getSubjectId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .publicSessionId(session.getPublicId())
                .publicProjectId(session.getPublicProjectId())
                .lastAccessedAt(Instant.now())
                .accessToken(claims.getAccessToken())
                .accessTokenExpiresAt(claims.getExpires_at())
                .build();
    }

    @Transactional
    public SessionDTO login_email_password(PasswordProjectLoginRequestDTO request, HttpServletRequest servletRequest,
                                                                HttpServletResponse response) {

        Project project = projectRepo.findByPublicProjectId(request.getPublicProjectId()).orElseThrow(RuntimeException::new);
        ProjectUser user = projectUserRepo.findByEmailAndProject(request.getEmail(),project)
                .orElseThrow(RuntimeException::new);
        if(user.getPassword()==null){
            throw new AlreadyExistsException();
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentailsException();
        }
        if(!user.isActive()){
            throw new AccountSuspendedEXception();
        }
        RefreshResult refreshResult= sessionService.createSession(user.getAuthifyerId() , request.getPublicProjectId() ,servletRequest,response);

        Session session =refreshResult.getSession();
        RefreshCookie.set(response, refreshResult.getRawRefreshToken());
        AccessTokenClaims claims =tokenService.issueAccessToken(refreshResult.getRawRefreshToken());

        return SessionDTO.builder()
                .subjectId(user.getSubjectId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .publicSessionId(session.getPublicId())
                .publicProjectId(session.getPublicProjectId())
                .lastAccessedAt(Instant.now())
                .accessToken(claims.getAccessToken())
                .accessTokenExpiresAt(claims.getExpires_at())
                .build();
    }


    @Transactional
    public void hardDelete(AuthPrincipal principal,HttpServletResponse response) {
        ProjectUser user =projectUserRepo.findByAuthifyerId(principal.getSubjectId()).orElseThrow(RuntimeException::new);
        List<Session> sessionList = sessionRepo.findBySubjectId(principal.getSubjectId());
        OAuthStorage oAuthStorage = oAuthStorageRepo.findBySubjectId(principal.getSubjectId());
        if(oAuthStorage!=null) {
            oAuthStorageRepo.delete(oAuthStorage);
        }
        RefreshCookie.clear(response);
        sessionRepo.deleteAll(sessionList);
        projectUserRepo.delete(user);
    }
}
