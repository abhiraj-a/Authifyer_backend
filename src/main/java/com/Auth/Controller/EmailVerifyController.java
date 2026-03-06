package com.Auth.Controller;
import com.Auth.DTO.SessionDTO;
import com.Auth.DTO.VerifyEmailRequest;
import com.Auth.Service.EmailService;
import com.Auth.Service.GlobalUserService;
import com.Auth.Service.ProjectUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request, HttpServletRequest servletRequest, HttpServletResponse response){


        log.warn("Verify email initiated");

//        SessionDTO sessionDTO = null;
//        if(request.getSubjectId().startsWith("auth_usr")){
//            log.warn("Project user signup initiated");
//           sessionDTO= projectUserService.makeSessionAfterSignup(request,servletRequest,response);
//        }
//        else {
//            log.warn("Global user signup initiated");
//            sessionDTO= globalUserService.makeSessionAfterSignup(request,servletRequest,response);
//        }
        return ResponseEntity.ok(emailService.verifyEmail(request,servletRequest,response));
    }

}
