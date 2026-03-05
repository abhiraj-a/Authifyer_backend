package com.Auth.Service;

import com.Auth.DTO.VerifyEmailRequest;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.ProjectUser;
import com.Auth.Entity.VerificationToken;
import com.Auth.Exception.ApiException;
import com.Auth.Exception.UserNotFoundException;
import com.Auth.Exception.VerificationTokenNotFound;
import com.Auth.Exception.VerifictionTokenExpired;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectUserRepo;
import com.Auth.Repo.VerificationTokenRepo;
import com.Auth.Util.VerifyUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;
private final VerificationTokenRepo verificationTokenRepo;
    private final RestTemplate restTemplate;

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
    public void verifyEmail(VerifyEmailRequest request) {
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
        if(subjectId.startsWith("glob_usr_")){
            GlobalUser user = globalUserRepo.findBySubjectId(subjectId).orElseThrow(UserNotFoundException::new);
            user.setEmailVerified(true);
            globalUserRepo.save(user);
            log.info("email verified : " + user.getEmail() + " now deleting the token");
            verificationTokenRepo.delete(vt);
        } else if (subjectId.startsWith("auth_usr_")) {
            ProjectUser user = projectUserRepo.findByAuthifyerId(subjectId).orElseThrow(UserNotFoundException::new);
            user.setEmailVerified(true);
            projectUserRepo.save(user);
            log.info("email verified :" + user.getEmail() + " now deleting the token");
            verificationTokenRepo.delete(vt);
        }

    }
}
