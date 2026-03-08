package com.Auth.Service;

import com.Auth.DTO.SessionDTO;
import com.Auth.DTO.VerifyEmailRequest;
import com.Auth.Entity.*;
import com.Auth.Exception.*;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.Repo.*;
import com.Auth.Util.RefreshResult;
import com.Auth.Util.TokenHash;
import com.Auth.Util.VerifyUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final TempUserStorageRepo tempUserStorageRepo;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;
    private final ProjectRepo projectRepo;
    private final VerificationTokenRepo verificationTokenRepo;
    private final RestTemplate restTemplate;
    private final TokenHash tokenHash;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${mail.sender.email}")
    private String senderEmail;

    // ================= SEND EMAIL =================
    @Async
    @Transactional
    public void sendVerificationEmail(
            String toEmail,
            String name,
            String verificationToken
    ) {

        log.warn("send verification mail method reached");

        String body = """
        {
          "sender":{
            "name":"Authifyer",
            "email":"%s"
          },
          "to":[{"email":"%s"}],
          "subject":"Verify your email for Authifyer",
          "htmlContent":"<h2>Hello %s</h2><p>Your verification token:</p><b>%s  Please enter this token to complete your verification</b>"
        }
        """.formatted(
                senderEmail,
                toEmail,
                name,
                verificationToken
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "https://api.brevo.com/v3/smtp/email",
                        request,
                        String.class
                );

        log.info("Brevo response: {}", response.getBody());
        log.info("Verification email sent to {}", toEmail);
    }

    // ================= TOKEN CREATION =================
    @Transactional
    public <T extends VerifyUser> void createVerificationToken(String name , String email,String subjectId) {
        log.warn("email verify reached");
        SecureRandom secureRandom =new SecureRandom();
        int num =100000+secureRandom.nextInt(900000);
        String token = String.valueOf(num);

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .verificationToken(token)
                        .subjectId(subjectId)
                        .expiresAt(
                                Instant.now()
                                        .plus(20, ChronoUnit.MINUTES))
                        .build();

        verificationTokenRepo.saveAndFlush(verificationToken);

        sendVerificationEmail(
                email,
                name,
                token
        );
    }

    @Transactional
    public SessionDTO verifyEmail(VerifyEmailRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        VerificationToken vt= verificationTokenRepo.findBySubjectId(request.getSubjectId())
                .orElseThrow(VerificationTokenNotFound::new);
        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenRepo.delete(vt);
            throw new VerifictionTokenExpired();
        }
        if (vt.getAttempts() >= 5) {
            verificationTokenRepo.delete(vt);
            throw new ApiException("Max verification attempts reached. Please request a new token.", HttpStatus.BAD_REQUEST);
        }
        if (!vt.getVerificationToken().equals(request.getToken())) {
            vt.setAttempts(vt.getAttempts() + 1);
            verificationTokenRepo.saveAndFlush(vt);
            throw new ApiException("Invalid verification code.", HttpStatus.BAD_REQUEST);
        }

        String subjectId= vt.getSubjectId();
        if(!subjectId.equals(request.getSubjectId())){
            log.warn("Subject id did not match");
            throw new ApiException("Invalid" , HttpStatus.BAD_REQUEST);
        }

            SessionDTO sessionDTO = null;
            if (request.getSubjectId().startsWith("auth_usr")) {
                log.warn("Project user signup initiated");


                log.warn("Session after signup activated");
                TempUserStorage tempUserStorage = tempUserStorageRepo.findBySubjectId(request.getSubjectId());
                ProjectUser user =ProjectUser.builder()
                        .emailVerified(true)
                        .name(tempUserStorage.getName())
                        .authifyerId(tempUserStorage.getSubjectId())
                        .email(tempUserStorage.getEmail())
                        .password(tokenHash.hash(tempUserStorage.getPassword()))
                        .createdAt(Instant.now())
                        .project(tempUserStorage.getProject())
                        .build();

                if(!tempUserStorage.getSubjectId().equals(user.getSubjectId())){
                    throw new ApiException("Invalid " , HttpStatus.BAD_REQUEST);
                }
                RefreshResult refreshResult = sessionService.createSession(user.getAuthifyerId(),  user.getProject().getPublishableKey(),servletRequest,response );
                Session session =refreshResult.getSession();
                AccessTokenClaims claims = tokenService.issueAccessToken_WhileSignup(refreshResult.getRawRefreshToken());
                Project  project = projectRepo.findByPublishableKey(user.getProject().getPublishableKey()).orElseThrow(ProjectNotFoundException::new);
                project.getProjectUsers().add(user);
                log.warn("Saving project user : "+user.getEmail());
                projectUserRepo.saveAndFlush(user);
                projectRepo.saveAndFlush(project);
                log.warn("Deleting temporary user: " + user.getEmail());
                tempUserStorageRepo.delete(tempUserStorage);

                sessionDTO= SessionDTO.builder()
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
            } else {
                log.warn("Global user signup initiated");
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
                sessionDTO= SessionDTO.builder()
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

//        if(subjectId.startsWith("glob_usr_")){
//            GlobalUser user = globalUserRepo.findBySubjectId(subjectId).orElseThrow(UserNotFoundException::new);
//            user.setEmailVerified(true);
//            globalUserRepo.save(user);
//            log.info("email verified : " + user.getEmail() + " now deleting the token");
//            verificationTokenRepo.delete(vt);
//        } else if (subjectId.startsWith("auth_usr_")) {
//            ProjectUser user = projectUserRepo.findByAuthifyerId(subjectId).orElseThrow(UserNotFoundException::new);
//            user.setEmailVerified(true);
//            projectUserRepo.save(user);
//            log.info("email verified :" + user.getEmail() + " now deleting the token");
//            verificationTokenRepo.delete(vt);
//        }

            return sessionDTO;
        }
    }

