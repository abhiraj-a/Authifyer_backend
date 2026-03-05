package com.Auth.Service;
import com.Auth.DTO.*;
import com.Auth.Entity.*;
import com.Auth.Exception.*;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.*;
import com.Auth.Util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
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
    private final TempUserStorageRepo tempUserStorageRepo;

    @Transactional
    public Map<String, String> signup_email_password(PasswordProjectRegisterRequest request , HttpServletRequest servletRequest,
                                                     HttpServletResponse response) {

        log.warn("project signup reached ");
        Project project = projectRepo.findByPublishableKey(request.getPublishableKey()).orElseThrow(ProjectNotFoundException::new);


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
//        projectUserRepo.saveAndFlush(projectUser);
        TempUserStorage tempUserStorage = TempUserStorage.builder()
                .projectUser(projectUser)
                .subjectId(projectUser.getSubjectId())
                .build();
        tempUserStorageRepo.saveAndFlush(tempUserStorage);
        emailService.createVerificationToken(projectUser);
//        projectRepo.save(project);

        return Map.of("subjectId" ,projectUser.getSubjectId());
    }

    @Transactional
    public SessionDTO login_email_password(PasswordProjectLoginRequestDTO request, HttpServletRequest servletRequest,
                                                                HttpServletResponse response) {

        Project project = projectRepo.findByPublishableKey(request.getPublishableKey()).orElseThrow(ProjectNotFoundException::new);
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
        RefreshResult refreshResult= sessionService.createSession(user.getAuthifyerId() , request.getPublishableKey() ,servletRequest,response);

        Session session =refreshResult.getSession();
//        RefreshCookie.set(response, refreshResult.getRawRefreshToken());
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
                .refreshToken(refreshResult.getRawRefreshToken())
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
//        RefreshCookie.clear(response);
        sessionRepo.deleteAll(sessionList);
        projectUserRepo.delete(user);
    }

    public SessionDTO makeSessionAfterSignup(VerifyEmailRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {

        TempUserStorage tempUserStorage = tempUserStorageRepo.findBySubjectId(request.getSubjectId());
        ProjectUser user =tempUserStorage.getProjectUser();
        if(!tempUserStorage.getSubjectId().equals(user.getSubjectId())){
            throw new ApiException("Invalid " , HttpStatus.BAD_REQUEST);
        }
        RefreshResult refreshResult = sessionService.createSession(user.getAuthifyerId(),  user.getProject().getPublishableKey(),servletRequest,response );
        Session session =refreshResult.getSession();
        AccessTokenClaims claims = tokenService.issueAccessToken(refreshResult.getRawRefreshToken());
        Project  project = projectRepo.findByPublishableKey(user.getProject().getPublishableKey()).orElseThrow(ProjectNotFoundException::new);
        project.getProjectUsers().add(user);
        log.warn("Saving project user : "+user.getEmail());
        projectUserRepo.saveAndFlush(user);
        projectRepo.saveAndFlush(project);
        log.warn("Deleting temporary user: " + user.getEmail());
        tempUserStorageRepo.delete(tempUserStorage);

        return SessionDTO.builder()
                .createdAt(Instant.now())
                .accessToken(claims.getAccessToken())
                .refreshToken(refreshResult.getRawRefreshToken())
                .subjectId(user.getSubjectId())
                .publicSessionId(session.getPublicId())
                .publicProjectId(session.getPublicProjectId())
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .user(SessionDTO.UserDTO.builder()
                        .name(user.getName())
                        .emailVerified(user.isEmailVerified())
                        .email(user.getEmail())
                        .subjectId(user.getSubjectId())
                        .build())
                .build();
    }
}
