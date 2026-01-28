package com.Auth.Controller;

import com.Auth.DTO.LoginRequest;
import com.Auth.DTO.RegisterRequest;
import com.Auth.Service.GlobalUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authifyer")
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


}
