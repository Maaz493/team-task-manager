package com.taskmanager.team_task_manager.controller;

import com.taskmanager.team_task_manager.model.Project;
import com.taskmanager.team_task_manager.model.User;
import com.taskmanager.team_task_manager.repository.ProjectRepository;
import com.taskmanager.team_task_manager.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;

    @Data
    static class ProjectRequest {
        @NotBlank String name;
        String description;
    }

    @Data
    static class AddMemberRequest {
        @NotBlank String memberId;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<?> getProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<Project> projects = projectRepository
                .findByOwnerIdOrMemberIdsContaining(user.getId(), user.getId());
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequest req,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setOwnerId(user.getId());
        projectRepository.save(project);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(user.getId()) &&
                !project.getMemberIds().contains(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMember(@PathVariable String id,
                                       @Valid @RequestBody AddMemberRequest req,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(owner.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only project owner can add members"));
        }

        if (!project.getMemberIds().contains(req.getMemberId())) {
            project.getMemberIds().add(req.getMemberId());
            projectRepository.save(project);
        }
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeMember(@PathVariable String id,
                                          @PathVariable String memberId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(owner.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only project owner can remove members"));
        }

        project.getMemberIds().remove(memberId);
        projectRepository.save(project);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable String id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(owner.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only project owner can delete"));
        }

        projectRepository.delete(project);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }

    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}