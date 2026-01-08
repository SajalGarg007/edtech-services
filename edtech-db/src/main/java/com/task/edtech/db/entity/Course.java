package com.task.edtech.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity
        implements Serializable {

    private static final long serialVersionUID = -5075736936745304781L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @NotNull(message = "Provider is required")
    private Provider provider;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Category is required")
    private CourseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Mode is required")
    private CourseMode mode;

    @Column
    private String address;

    @Column(name = "pin_code")
    private String pinCode;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "schedule_info")
    @Size(max = 100, message = "Schedule info must not exceed 100 characters")
    private String scheduleInfo;

    @Column(name = "price_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal priceAmount;

    @Column(name = "is_free", nullable = false)
    @NotNull
    private Boolean isFree = false;

    @Column
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Column(name = "is_published", nullable = false)
    @NotNull
    private Boolean isPublished = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        generateInternalId();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @AssertTrue(message = "Address and PIN code are required for in-person courses")
    private boolean isValidInPersonCourse() {
        if (mode == CourseMode.IN_PERSON) {
            return address != null && !address.isBlank() &&
                    pinCode != null && !pinCode.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "Price amount is required for paid courses")
    private boolean isValidPrice() {
        if (!isFree && priceAmount == null) {
            return false;
        }
        if (isFree && priceAmount != null && priceAmount.compareTo(BigDecimal.ZERO) > 0) {
            return false;
        }
        return true;
    }
}

