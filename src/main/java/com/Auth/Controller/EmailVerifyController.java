package com.Auth.Controller;
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
import com.Auth.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/auth/verify-email")
@RequiredArgsConstructor
public class EmailVerifyController {
    private final VerificationTokenRepo verificationTokenRepo;
    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request){
        emailService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

}
