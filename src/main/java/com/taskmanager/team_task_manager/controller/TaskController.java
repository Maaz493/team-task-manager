package com.taskmanager.team_task_manager.controller;

import com.taskmanager.team_task_manager.model.Project;
import com.taskmanager.team_task_manager.model.Task;
import com.taskmanager.team_task_manager.model.User;
import com.taskmanager.team_task_manager.repository.ProjectRepository;
import com.taskmanager.team_task_manager.repository.TaskRepository;
import com.taskmanager.team_task_manager.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;

    @Data
    static class TaskRequest {
        @NotBlank String title;
        String description;
        String assignedToId;
        Task.Status status = Task.Status.TODO;
        Task.Priority priority = Task.Priority.MEDIUM;
        LocalDateTime dueDate;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    private boolean hasAccess(Project project, String userId) {
        return project.getOwnerId().equals(userId) ||
                project.getMemberIds().contains(userId);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable String projectId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!hasAccess(project, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(taskRepository.findByAssignedToId(user.getId()));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> createTask(@PathVariable String projectId,
                                        @Valid @RequestBody TaskRequest req,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!hasAccess(project, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setProjectId(projectId);
        task.setAssignedToId(req.getAssignedToId());
        task.setCreatedById(user.getId());
        task.setStatus(req.getStatus() != null ? req.getStatus() : Task.Status.TODO);
        task.setPriority(req.getPriority() != null ? req.getPriority() : Task.Priority.MEDIUM);
        task.setDueDate(req.getDueDate());
        taskRepository.save(task);

        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id,
                                        @RequestBody TaskRequest req,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = projectRepository.findById(task.getProjectId()).orElseThrow();
        if (!hasAccess(project, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getAssignedToId() != null) task.setAssignedToId(req.getAssignedToId());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getPriority() != null) task.setPriority(req.getPriority());
        if (req.getDueDate() != null) task.setDueDate(req.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestBody Map<String, String> body,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = projectRepository.findById(task.getProjectId()).orElseThrow();
        if (!hasAccess(project, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        task.setStatus(Task.Status.valueOf(body.get("status")));
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = projectRepository.findById(task.getProjectId()).orElseThrow();
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isCreator = task.getCreatedById().equals(user.getId());

        if (!isAdmin && !isCreator) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Not authorized to delete"));
        }

        taskRepository.delete(task);
        return ResponseEntity.ok(Map.of("message", "Task deleted"));
    }
}