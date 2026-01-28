package com.Auth.Controller;

import com.Auth.DTO.ProjectCreationRequest;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authifyer/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    @PostMapping("/create")
    public ResponseEntity<?> createProject(@AuthenticationPrincipal AuthPrincipal principal , ProjectCreationRequest request){
        return ResponseEntity.ok(projectService.createProject(principal ,request ));
    }

    @GetMapping("/my-projects")
    public ResponseEntity<?> Projects(@AuthenticationPrincipal AuthPrincipal principal){
        return ResponseEntity.ok(projectService.loadProjects(principal));
    }

    @GetMapping("/project/{publicId}")
    public ResponseEntity<?> getProject(@AuthenticationPrincipal AuthPrincipal principal , @PathVariable String publicId){
        return ResponseEntity.ok(projectService.getProject(principal,publicId));
    }

}
