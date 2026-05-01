package com.taskmanager.team_task_manager.controller;

import com.taskmanager.team_task_manager.model.Project;
import com.taskmanager.team_task_manager.model.Task;
import com.taskmanager.team_task_manager.model.User;
import com.taskmanager.team_task_manager.repository.ProjectRepository;
import com.taskmanager.team_task_manager.repository.TaskRepository;
import com.taskmanager.team_task_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<Project> projects = projectRepository
                .findByOwnerIdOrMemberIdsContaining(user.getId(), user.getId());
        List<String> projectIds = projects.stream()
                .map(Project::getId).collect(Collectors.toList());

        List<Task> allTasks = taskRepository.findByProjectIdIn(projectIds);
        List<Task> myTasks = taskRepository.findByAssignedToId(user.getId());
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalProjects", projects.size());
        dashboard.put("totalTasks", allTasks.size());
        dashboard.put("todoTasks", allTasks.stream()
                .filter(t -> t.getStatus() == Task.Status.TODO).count());
        dashboard.put("inProgressTasks", allTasks.stream()
                .filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count());
        dashboard.put("doneTasks", allTasks.stream()
                .filter(t -> t.getStatus() == Task.Status.DONE).count());
        dashboard.put("overdueTasks", allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(now)
                        && t.getStatus() != Task.Status.DONE).count());
        dashboard.put("myAssignedTasks", myTasks.size());
        dashboard.put("recentTasks", allTasks.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5).collect(Collectors.toList()));
        dashboard.put("projects", projects);
        dashboard.put("currentUser", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));

        return ResponseEntity.ok(dashboard);
    }
}