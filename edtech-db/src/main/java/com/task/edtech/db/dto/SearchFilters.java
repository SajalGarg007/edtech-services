package com.task.edtech.db.dto;

import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {

    private String pinCode;
    private CourseCategory category;
    private CourseMode mode;
    private Boolean isFree;
    private LocalDate startFrom;
    private LocalDate startTo;
}

