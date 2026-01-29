package com.Auth.Controller;

import com.Auth.DTO.LoginRequest;
import com.Auth.DTO.RegisterRequest;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Service.GlobalUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authifyer/global")
public class GlobalUserController {

    private final GlobalUserService globalUserService;
    @PostMapping("/signup")
    public ResponseEntity<?> globalsigup(@RequestBody RegisterRequest request, HttpServletRequest servletRequest , HttpServletResponse response){
        return ResponseEntity.ok(globalUserService.signup(request,servletRequest,response));
    }

    @PostMapping("/login")
    public  ResponseEntity<?> globalLogin(@RequestBody LoginRequest request, HttpServletRequest servletRequest , HttpServletResponse response){
        return ResponseEntity.ok(globalUserService.login( request, servletRequest  , response));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@AuthenticationPrincipal AuthPrincipal principal){
      globalUserService.softdelete(principal);
      return ResponseEntity.ok().build();
    }
}
