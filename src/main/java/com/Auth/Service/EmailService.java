package com.Auth.Service;

import com.Auth.Entity.VerificationToken;
import com.Auth.Repo.VerificationTokenRepo;
import com.Auth.Util.VerifyUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final VerificationTokenRepo verificationTokenRepo;
    private final JavaMailSender mailSender;
    private final String senderEmail;
    @Async
    private void sendVerificationEmail(String toEmail, String name, String verificationLink) {
     try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(mimeMessage);
         helper.setText(buildHtmlContent(name, verificationLink), true);
         helper.setTo(toEmail);
         helper.setSubject("Verify your email for Authifyer");
         helper.setFrom(senderEmail);
         mailSender.send(mimeMessage);
         log.info("Verification email sent to {}", toEmail);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildHtmlContent(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "  <div style=\"background-color:#ffffff;max-width:580px;margin:0 auto\">\n" +
                "    <div style=\"padding: 20px; text-align: center;\">\n" +
                "       <h2>Welcome to Authifyer, " + name + "!</h2>\n" +
                "       <p>Please click the button below to verify your account:</p>\n" +
                "       <a href=\"" + link + "\" style=\"background-color:#1a82e2;border-radius:4px;color:#ffffff;display:inline-block;font-weight:bold;line-height:40px;text-align:center;text-decoration:none;width:200px\">Verify Email</a>\n" +
                "       <p style=\"font-size: 12px; color: #666; margin-top: 20px;\">Link expires in 24 hours.</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>";
    }

    public   <T extends VerifyUser> void createVerificationToken(T user){
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .verificationToken(token)
                .subjectId(user.getSubjectId())
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .build();

        verificationTokenRepo.save(verificationToken);
        String verifyUrl = "/api/auth/verify-email?token="+token;
        sendVerificationEmail(user.getEmail() , user.getName(),verifyUrl);

    }
}
