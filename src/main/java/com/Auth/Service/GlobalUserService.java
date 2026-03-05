package com.Auth.Service;
import com.Auth.DTO.*;
import com.Auth.Entity.*;
import com.Auth.Exception.AccountSuspendedEXception;
import com.Auth.Exception.AlreadyExistsException;
import com.Auth.Exception.InvalidCredentailsException;
import com.Auth.Exception.UserNotFoundException;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Repo.*;
import com.Auth.Util.IdGenerator;
import com.Auth.Util.RefreshResult;
import com.Auth.Util.TokenHash;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalUserService {

    private final GlobalUserRepo globalUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final SessionRepo sessionRepo;
    private final TempUserStorageRepo tempUserStorageRepo;

    @Transactional
    public Map<String, String> signup(RegisterRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {

        GlobalUser user;
        user = globalUserRepo.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            throw new AlreadyExistsException();
        }

        user = GlobalUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .subjectId(IdGenerator.generateGlobalUserSubjectId())
                .name(request.getName())
                .build();

        TempUserStorage tempUserStorage = TempUserStorage.builder()
                .subjectId(IdGenerator.generateAuthifyerId())
                .project(null)
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build();

        tempUserStorageRepo.saveAndFlush(tempUserStorage);

        emailService.createVerificationToken(tempUserStorage.getName(),tempUserStorage.getEmail(),tempUserStorage.getSubjectId());


//        RefreshResult refreshResult =sessionService.createGlobalSession(user.getSubjectId()
//                                   ,servletRequest,response);
//
//        Session session=refreshResult.getSession();

//        AccessTokenClaims claims = tokenService.issueGlobalAccessToken(refreshResult.getRawRefreshToken());
        return Map.of("subjectId", user.getSubjectId());
    }


    @Transactional
    public SessionDTO login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        GlobalUser user = globalUserRepo.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }

        if (!user.isActive()) {
            throw new AccountSuspendedEXception();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentailsException();
        }
        RefreshResult refreshResult = sessionService.createGlobalSession(user.getSubjectId(), servletRequest, response);
        Session session = refreshResult.getSession();
        AccessTokenClaims claims = tokenService.issueGlobalAccessToken(refreshResult.getRawRefreshToken());
        return SessionDTO.builder()
                .subjectId(session.getSubjectId())
                .publicSessionId(session.getPublicId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .accessToken(claims.getAccessToken())
                .accessTokenExpiresAt(claims.getExpires_at())
                .refreshToken(refreshResult.getRawRefreshToken())
                .user(SessionDTO.UserDTO.builder()
                        .name(user.getName())
                        .provider(user.getProvider())
                        .email(user.getEmail())
                        .emailVerified(user.isEmailVerified())
                        .build())
                .build();
    }

    @Transactional
    public void softdelete(AuthPrincipal principal) {
        GlobalUser user = globalUserRepo.findBySubjectId(principal.getSubjectId()).orElseThrow(UserNotFoundException::new);
        user.setActive(false);
        globalUserRepo.save(user);
        List<Session> sessions = sessionRepo.findAllBySubjectIdAndRevokedAtIsNull(principal.getSubjectId());
        Instant now = Instant.now();

        for (Session session : sessions) {
            session.setRevokedAt(now);
        }
        sessionRepo.saveAll(sessions);
    }

    public SessionDTO makeSessionAfterSignup(VerifyEmailRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {


            TokenHash tokenHash=new TokenHash();

            TempUserStorage tempUserStorage = tempUserStorageRepo.findBySubjectId(request.getSubjectId());
            GlobalUser user = GlobalUser.builder()
                    .emailVerified(true)
                    .name(tempUserStorage.getName())
                    .subjectId(tempUserStorage.getSubjectId())
                    .email(tempUserStorage.getEmail())
                    .password(tokenHash.hash(tempUserStorage.getPassword()))
                    .createdAt(Instant.now())
                    .build();
            RefreshResult refreshResult = sessionService.createGlobalSession(user.getSubjectId(), servletRequest, response);
            Session session = refreshResult.getSession();
            AccessTokenClaims claims = tokenService.issueGlobalAccessToken(refreshResult.getRawRefreshToken());
            log.warn("Saving global user: "+user.getEmail());
            globalUserRepo.saveAndFlush(user);
            tempUserStorageRepo.delete(tempUserStorage);
            log.warn("Deleting temporary user: " + user.getEmail());
            return SessionDTO.builder()
                    .publicProjectId(null)
                    .publicSessionId(session.getPublicId())
                    .lastAccessedAt(Instant.now())
                    .subjectId(user.getSubjectId())
                    .accessToken(claims.getAccessToken())
                    .refreshToken(refreshResult.getRawRefreshToken())
                    .user(SessionDTO.UserDTO.builder()
                            .name(user.getName())
                            .provider(user.getProvider())
                            .email(user.getEmail())
                            .emailVerified(user.isEmailVerified())
                            .build())
                    .build();
        }
    }

