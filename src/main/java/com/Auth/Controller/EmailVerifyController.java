package com.Auth.Controller;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.ProjectUser;
import com.Auth.Entity.VerificationToken;
import com.Auth.Exception.UserNotFoundException;
import com.Auth.Exception.VerificationTokenNotFound;
import com.Auth.Exception.VerifictionTokenExpired;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.ProjectUserRepo;
import com.Auth.Repo.VerificationTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth/verify-email")
@RequiredArgsConstructor
public class EmailVerifyController {
    private final VerificationTokenRepo verificationTokenRepo;
    private final GlobalUserRepo globalUserRepo;
    private final ProjectUserRepo projectUserRepo;

    @PostMapping
    public ResponseEntity<?> verifyEmail(@RequestBody String token){
        VerificationToken vt= verificationTokenRepo.findByVerificationToken(token)
                .orElseThrow(VerificationTokenNotFound::new);
        if(vt.getExpiresAt().isBefore(Instant.now())) throw new VerifictionTokenExpired();
        String subjectId= vt.getSubjectId();
        if(subjectId.startsWith("glob_usr_")){
            GlobalUser user = globalUserRepo.findBySubjectId(subjectId).orElseThrow(UserNotFoundException::new);
            user.setEmailVerified(true);
            globalUserRepo.save(user);
            return ResponseEntity.ok().build();
        } else if (subjectId.startsWith("auth_usr_")) {
            ProjectUser user = projectUserRepo.findByAuthifyerId(subjectId).orElseThrow(UserNotFoundException::new);
            user.setEmailVerified(true);
            projectUserRepo.save(user);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
