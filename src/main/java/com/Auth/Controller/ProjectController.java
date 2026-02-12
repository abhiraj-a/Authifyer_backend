package com.Auth.Controller;

import com.Auth.DTO.ProjectCreationRequest;
import com.Auth.Principal.AuthPrincipal;
import com.Auth.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authifyer/global/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )    public ResponseEntity<?> createProject(@AuthenticationPrincipal AuthPrincipal principal ,@RequestBody ProjectCreationRequest request){
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

    @PostMapping("/project/{publicId}/{authifyerId}/toggle-status")
    public ResponseEntity<?>  toggleUserStatus(@AuthenticationPrincipal AuthPrincipal principal , @PathVariable String publicId,
                                           @PathVariable String authifyerId){
        projectService.toggleUser(principal,publicId,authifyerId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/project/{publicId}/{authifyerId}/delete")
    public ResponseEntity<?>  deleteUser(@AuthenticationPrincipal AuthPrincipal principal , @PathVariable String publicId,
                                               @PathVariable String authifyerId){
        projectService.deleteUser(principal,publicId,authifyerId);
        return ResponseEntity.ok().build();
    }

}
