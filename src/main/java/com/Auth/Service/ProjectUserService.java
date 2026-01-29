package com.Auth.Service;
import com.Auth.DTO.*;
import com.Auth.Entity.*;
import com.Auth.Repo.ProjectRepo;
import com.Auth.Repo.ProjectUserRepo;
import com.Auth.Repo.VerificationTokenRepo;
import com.Auth.Util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectUserService {


    private final SessionService sessionService;
    private final ProjectUserRepo projectUserRepo;
    private final TokenService tokenService;
    private final ProjectRepo projectRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepo verificationTokenRepo;

    public SessionDTO signup_email_password(PasswordProjectRegisterRequest request , HttpServletRequest servletRequest,
                                            HttpServletResponse response) {

        Project project = projectRepo.findByPublicProjectId(request.getPublicProjectId()).orElseThrow(RuntimeException::new);

        if(projectUserRepo.existsByPublicProjectIdAndEmail(request.getPublicProjectId(),request.getEmail()))
            throw new RuntimeException("Email already Exists");

        ProjectUser projectUser = projectUserRepo.save(ProjectUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .project(project)
                .authifyerId(IdGenerator.generateAuthifyerId())
                .createdAt(Instant.now())
                .build());

        emailService.createVerificationToken(projectUser);


        RefreshResult refreshResult = sessionService.createSession(projectUser.getAuthifyerId(),  project.getPublicId(),servletRequest,response );

        Session session =refreshResult.getSession();
        project.getProjectUsers().add(projectUser);
        projectRepo.save(project);

        return SessionDTO.builder()
                .subjectId(projectUser.getSubjectId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .publicSessionId(session.getPublicId())
                .publicProjectId(session.getPublicProjectId())
                .lastAccessedAt(Instant.now())
                .accessTokenClaims(tokenService.issueAccessToken(refreshResult.getRawRefreshToken()))
                .build();
    }

    public SessionDTO login_email_password(PasswordProjectLoginRequestDTO request, HttpServletRequest servletRequest,
                                                                HttpServletResponse response) {

        ProjectUser user = projectUserRepo.findByEmailAndPublicProjectId(request.getEmail(),request.getPublicProjectId())
                .orElseThrow(RuntimeException::new);
        if(user.getPassword()==null){
            throw new RuntimeException("User exists by oauth account please continue with" + user.getProvider());
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        RefreshResult refreshResult= sessionService.createSession(user.getAuthifyerId() , request.getPublicProjectId() ,servletRequest,response);

        Session session =refreshResult.getSession();
        RefreshCookie.set(response, refreshResult.getRawRefreshToken());
        return SessionDTO.builder()
                .subjectId(user.getSubjectId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .publicSessionId(session.getPublicId())
                .publicProjectId(session.getPublicProjectId())
                .lastAccessedAt(Instant.now())
                .accessTokenClaims(tokenService.issueAccessToken(refreshResult.getRawRefreshToken()))
                .build();
    }


}
