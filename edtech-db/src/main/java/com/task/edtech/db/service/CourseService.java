package com.task.edtech.db.service;

import com.task.edtech.db.dto.SearchFilters;
import com.task.edtech.db.entity.Course;
import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseService {

    Course findById(@NotNull Long courseId);

    Course findByInternalId(@NotNull UUID courseInternalId);

    Course findByUserIdAndTitle(
            @NotNull Long userId,
            @NotNull String title);

    long countByUserId(@NotNull Long userId);

    Course addOrUpdate(@NotNull Course course);

    void delete(@NotNull Course course);

    Course publishCourse(@NotNull UUID courseInternalId);

    Course unpublishCourse(@NotNull UUID courseInternalId);

    List<Course> getAllByUserId(@NotNull Long userId);

    Page<Course> getAllByUserId(
            @NotNull Long userId,
            @NotNull Pageable pageable);

    List<Course> getAllByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull String pinPrefix,
            @NotNull LocalDate date);

    List<Course> getAllByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull String pinCode,
            @NotNull LocalDate date);

    Page<Course> getAllByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull CourseCategory category,
            @NotNull LocalDate date,
            @NotNull Pageable pageable);

    Page<Course> getAllByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull CourseMode mode,
            @NotNull LocalDate date,
            @NotNull Pageable pageable);

    Page<Course> getAllByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull Boolean isFree,
            @NotNull LocalDate date,
            @NotNull Pageable pageable);

    List<Course> searchCourses(
            @Nullable String pinCode,
            @Nullable SearchFilters filters);
}
