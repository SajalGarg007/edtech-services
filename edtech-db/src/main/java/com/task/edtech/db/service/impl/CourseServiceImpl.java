package com.task.edtech.db.service.impl;

import com.task.edtech.db.dto.SearchFilters;
import com.task.edtech.db.entity.Course;
import com.task.edtech.db.entity.User;
import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import com.task.edtech.db.exception.EntityNotFoundException;
import com.task.edtech.db.repository.CourseRepository;
import com.task.edtech.db.service.CourseService;
import com.task.edtech.db.service.UserService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Course findById(@NotNull Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(
                () -> new EntityNotFoundException("Course not found with Id: " + courseId));

    }

    @Override
    public Course findByInternalId(@NotNull UUID courseInternalId) {
        return courseRepository.findByInternalId(courseInternalId).orElseThrow(
                () -> new EntityNotFoundException("Course not found with internalId" + courseInternalId));
    }

    @Override
    public Course findByUserIdAndTitle(
            @NotNull Long userId,
            @NotNull String title) {
        return courseRepository.findByUserIdAndTitle(userId, title).orElseThrow(
                () -> new EntityNotFoundException("Course by user with this title already exist"));
    }

    @Override
    public long countByUserId(@NotNull Long userId) {
        return courseRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public Course addOrUpdate(@NotNull Course course) {
        User user = null;
        if (Objects.isNull(course.getUser())) {
            throw new RuntimeException("Course must have provider");
        } else if (Objects.nonNull(course.getUser().getInternalId())) {
            user = userService.findByInternalId(course.getUser().getInternalId());
            if (Objects.isNull(user) && Objects.nonNull(course.getUser().getId())) {
                user = userService.findById(course.getUser().getId());
            }
        }
        if (Objects.isNull(user)) {
            throw new RuntimeException("Create added user for this course");
        } else {
            course.setUser(user);
        }

        Course dbCourse = null;
        if (Objects.nonNull(course.getInternalId())) {
            dbCourse = courseRepository.findByInternalId(course.getInternalId())
                    .orElse(null);
        } else if (Objects.nonNull(course.getId())) {
            dbCourse = courseRepository.findById(course.getId())
                    .orElse(null);
        }

        if (Objects.isNull(dbCourse)) {
            dbCourse = courseRepository.findByUserIdAndTitle(course.getUser().getId(), course.getTitle())
                    .orElse(null);
        }

        if (Objects.nonNull(dbCourse)) {
            dbCourse.copy(course);
        } else {
            dbCourse = course;
        }

        log.info("adding/updating course with id %s", course.getId());
        courseRepository.save(dbCourse);
        log.info("added/updated course with id %s", course.getId());
        return dbCourse;
    }

    @Override
    @Transactional
    public void delete(@NotNull Course course) {
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public Course publishCourse(@NotNull UUID courseInternalId) {
        Course course = courseRepository.findByInternalId(courseInternalId).orElseThrow(
                () -> new EntityNotFoundException("Course not found with internalId" + courseInternalId));
        course.setIsPublished(true);
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course unpublishCourse(@NotNull UUID courseInternalId) {
        Course course = courseRepository.findByInternalId(courseInternalId).orElseThrow(
                () -> new EntityNotFoundException("Course not found with internalId" + courseInternalId));
        course.setIsPublished(false);
        return courseRepository.save(course);
    }

    @Override
    public List<Course> getAllByUserId(@NotNull Long userId) {
        return courseRepository.getAllByUserId(userId);
    }

    @Override
    public Page<Course> getAllByUserId(
            @NotNull Long userId,
            @NotNull Pageable pageable) {
        return courseRepository.getAllByUserId(userId, pageable);
    }

    @Override
    public List<Course> getAllByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull String pinPrefix,
            @NotNull LocalDate date) {
        return courseRepository.getAllByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
                pinPrefix, date);
    }

    @Override
    public List<Course> getAllByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull String pinCode,
            @NotNull LocalDate date) {
        return courseRepository.getAllByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
                pinCode, date);
    }

    @Override
    public Page<Course> getAllByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull CourseCategory category,
            @NotNull LocalDate date,
            @NotNull Pageable pageable) {
        return courseRepository.getAllByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
                category, date, pageable);
    }

    @Override
    public Page<Course> getAllByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull CourseMode mode,
            @NotNull LocalDate date,
            @NotNull Pageable pageable) {
        return courseRepository.getAllByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
                mode, date, pageable);
    }

    @Override
    public Page<Course> getAllByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @NotNull Boolean isFree,
            @NotNull LocalDate date,
            @NotNull Pageable pageable) {
        return courseRepository.getAllByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
                isFree, date, pageable);
    }

    @Override
    public Page<Course> searchCourses(
            @NotNull String pinCode,
            @Nullable SearchFilters filters,
            @NotNull Pageable pageable) {

        if (pinCode == null || pinCode.isBlank()) {
            throw new RuntimeException("PIN code is required");
        }

        LocalDate startFrom = filters != null && filters.getStartFrom() != null
                ? filters.getStartFrom()
                : LocalDate.now();

        String searchPinCode = filters != null && filters.getPinCode() != null
                ? filters.getPinCode()
                : pinCode;

        return courseRepository.searchCourses(
                searchPinCode,
                filters != null ? filters.getCategory() : null,
                filters != null ? filters.getMode() : null,
                filters != null ? filters.getIsFree() : null,
                startFrom,
                filters != null ? filters.getStartTo() : null,
                pageable
        );
    }
}
