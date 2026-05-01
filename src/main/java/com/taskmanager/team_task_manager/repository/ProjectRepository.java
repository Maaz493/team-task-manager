package com.taskmanager.team_task_manager.repository;

import com.taskmanager.team_task_manager.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByOwnerId(String ownerId);
    List<Project> findByMemberIdsContaining(String memberId);
    List<Project> findByOwnerIdOrMemberIdsContaining(String ownerId, String memberId);
}