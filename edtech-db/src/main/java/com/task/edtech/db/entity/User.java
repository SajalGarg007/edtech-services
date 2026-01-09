package com.task.edtech.db.entity;

import com.task.edtech.db.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_internal_id", columnNames = "internal_id"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_user_internal_id", columnList = "internal_id"),
        @Index(name = "idx_user_email", columnList = "email")
    })
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity
        implements Serializable {

    private static final long serialVersionUID = -8026564716164294260L;

    @Column(nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Password is required")
    private String passwordHash;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    @NotNull(message = "User type is required")
    private UserType userType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        generateInternalId(); // Generate UUID from BaseEntity
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void copy(User user) {
        if (user == null) {
            return;
        }

        this.email = user.email;
        this.passwordHash = user.passwordHash;
        this.name = user.name;
        this.userType = user.userType;
    }
}

