package com.task.edtech.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = -4924524433095414440L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internal_id", unique = true, nullable = false, updatable = false)
    private UUID internalId;

    @PrePersist
    protected void generateInternalId() {
        if (internalId == null) {
            internalId = UUID.randomUUID();
        }
    }
}

