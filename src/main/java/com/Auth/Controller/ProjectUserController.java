package com.Auth.Controller;
import com.Auth.DTO.*;
import com.Auth.Service.ProjectUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/authifyer")
@RequiredArgsConstructor
public class ProjectUserController {
    private final ProjectUserService projectUserService;


    @PostMapping("/register/email")
    public ResponseEntity<?> passwordRegistration(@RequestBody PasswordProjectRegisterRequest request){
        return ResponseEntity.ok(projectUserService.signup_email_password(request));
    }



    @PostMapping("/login/email")
    public ResponseEntity<?> passwordLogin(@RequestBody PasswordProjectLoginRequestDTO request , HttpServletRequest servletRequest,
                                           HttpServletResponse response){


        return ResponseEntity.
                ok(projectUserService.login_email_password(request,servletRequest,response));
    }


}
