package com.Auth.Controller;
import com.Auth.DTO.VerifyEmailRequest;
import com.Auth.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/verify-email")
@RequiredArgsConstructor
public class EmailVerifyController {
    private final EmailService emailService;
    @PostMapping
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request){
        emailService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

}
