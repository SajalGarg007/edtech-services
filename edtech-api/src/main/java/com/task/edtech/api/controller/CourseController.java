package com.task.edtech.api.controller;

import com.task.edtech.db.converter.CourseConverter;
import com.task.edtech.db.dto.CourseDTO;
import com.task.edtech.db.dto.SearchFilters;
import com.task.edtech.db.entity.Course;
import com.task.edtech.db.entity.User;
import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import com.task.edtech.db.enums.UserType;
import com.task.edtech.db.exception.EntityNotFoundException;
import com.task.edtech.db.service.AuthService;
import com.task.edtech.db.service.CourseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseConverter courseConverter;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        Long userId = authService.getCurrentUserId();
        Course course = courseConverter.toEntity(courseDTO, userId);
        User user = course.getUser();
        if (!Objects.equals(user.getUserType(), UserType.PROVIDER)) {
            throw new RuntimeException("Only provider could create courses");
        }
        Course dbCourse = courseService.addOrUpdate(course);
        CourseDTO responseDTO = courseConverter.toDto(dbCourse);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<CourseDTO>> getMyCourses() {
        Long userId = authService.getCurrentUserId();
        List<Course> courses = courseService.getAllByUserId(userId);
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable @NotNull Long id) {
        Long userId = authService.getCurrentUserId();
        Course course = courseService.findById(id);

        if (!course.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Course not found or access denied");
        }
        
        CourseDTO courseDTO = courseConverter.toDto(course);
        return ResponseEntity.ok(courseDTO);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        Long userId = authService.getCurrentUserId();

        Course existingCourse = courseService.findById(id);
        if (!existingCourse.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Course not found or access denied");
        }

        Course course = courseConverter.toEntity(courseDTO, userId);
        course.setIsPublished(existingCourse.getIsPublished());
        Course updatedCourse = courseService.addOrUpdate(course);
        CourseDTO responseDTO = courseConverter.toDto(updatedCourse);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable @NotNull UUID courseId) {
        Long userId = authService.getCurrentUserId();

        Course course = courseService.findByInternalId(courseId);
        if (!course.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Course not found or access denied");
        }
        
        courseService.delete(course);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/publish")
    public ResponseEntity<CourseDTO> publishCourse(@PathVariable @NotNull UUID courseId) {
        Long userId = authService.getCurrentUserId();

        Course course = courseService.findByInternalId(courseId);
        if (!course.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Course not found or access denied");
        }
        
        Course publishedCourse = courseService.publishCourse(course.getInternalId());
        CourseDTO courseDTO = courseConverter.toDto(publishedCourse);
        return ResponseEntity.ok(courseDTO);
    }

    @PostMapping("/{courseId}/unpublish")
    public ResponseEntity<CourseDTO> unpublishCourse(@PathVariable @NotNull UUID courseId) {
        Long userId = authService.getCurrentUserId();

        Course course = courseService.findByInternalId(courseId);
        if (!course.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Course not found or access denied");
        }
        
        Course unpublishedCourse = courseService.unpublishCourse(course.getInternalId());
        CourseDTO courseDTO = courseConverter.toDto(unpublishedCourse);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseDTO>> searchCourses(
            @RequestParam @NotNull String pinCode,
            @RequestParam(required = false) String filterPinCode,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) CourseMode mode,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir) {

        SearchFilters filters = new SearchFilters();
        filters.setPinCode(filterPinCode);
        filters.setCategory(category);
        filters.setMode(mode);
        filters.setIsFree(isFree);
        filters.setStartFrom(startFrom);
        filters.setStartTo(startTo);

        Sort sort = Sort.by(sortDir, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Course> courses = courseService.searchCourses(pinCode, filters, pageable);

        Page<CourseDTO> courseDTOs = courses.map(courseConverter::toDto);

        return ResponseEntity.ok(courseDTOs);
    }
}

