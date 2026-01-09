package com.task.edtech.db.dto;

import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private CourseCategory category;

    @NotNull(message = "Mode is required")
    private CourseMode mode;

    private String address;

    private String pinCode;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100, message = "Schedule info must not exceed 100 characters")
    private String scheduleInfo;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal priceAmount;

    @NotNull
    private Boolean isFree = false;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
