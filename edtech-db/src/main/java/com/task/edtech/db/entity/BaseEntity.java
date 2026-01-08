package com.task.edtech.db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
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

