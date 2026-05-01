package com.taskmanager.team_task_manager.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Email
    @Indexed(unique = true)
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private Role role = Role.MEMBER;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, MEMBER
    }
}
