package com.taskmanager.team_task_manager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    private String ownerId;

    private List<String> memberIds = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}
