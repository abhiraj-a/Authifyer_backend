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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request){
        VerificationToken vt= verificationTokenRepo.findByVerificationToken(request.getToken())
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
            verificationTokenRepo.save(vt);
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
            log.warn("email verified " + user.getEmail());
            return ResponseEntity.ok().build();
        } else if (subjectId.startsWith("auth_usr_")) {
            ProjectUser user = projectUserRepo.findByAuthifyerId(subjectId).orElseThrow(UserNotFoundException::new);
            user.setEmailVerified(true);
            projectUserRepo.save(user);
            log.warn("email verified " + user.getEmail());
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
