package com.Auth.Service;

import com.Auth.Entity.VerificationToken;
import com.Auth.Repo.VerificationTokenRepo;
import com.Auth.Util.VerifyUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

private final VerificationTokenRepo verificationTokenRepo;
    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${mail.sender.email}")
    private String senderEmail;

    // ================= SEND EMAIL =================
    @Async
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
          "htmlContent":"<h2>Hello %s</h2><p>Your verification token:</p><b>%s</b>"
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
    public <T extends VerifyUser> void createVerificationToken(T user) {

        log.warn("email verify reached");

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .verificationToken(token)
                        .subjectId(user.getSubjectId())
                        .expiresAt(
                                Instant.now()
                                        .plus(20, ChronoUnit.MINUTES))
                        .build();

        verificationTokenRepo.save(verificationToken);

        sendVerificationEmail(
                user.getEmail(),
                user.getName(),
                token
        );
    }
}
