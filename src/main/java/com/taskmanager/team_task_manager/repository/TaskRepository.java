package com.taskmanager.team_task_manager.repository;

import com.taskmanager.team_task_manager.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByProjectId(String projectId);
    List<Task> findByAssignedToId(String userId);
    List<Task> findByProjectIdAndStatus(String projectId, Task.Status status);
    List<Task> findByAssignedToIdAndDueDateBefore(String userId, LocalDateTime date);
    List<Task> findByProjectIdIn(List<String> projectIds);
}