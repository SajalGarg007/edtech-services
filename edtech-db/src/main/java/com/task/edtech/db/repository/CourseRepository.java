package com.task.edtech.db.repository;

import com.task.edtech.db.entity.Course;
import com.task.edtech.db.entity.CourseCategory;
import com.task.edtech.db.entity.CourseMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByProviderId(Long providerId);

    Page<Course> findByProviderId(Long providerId, Pageable pageable);

    List<Course> findByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
            String pinPrefix, LocalDate date);
    

    List<Course> findByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            String pinCode, LocalDate date);

    @Query("SELECT c FROM Course c WHERE " +
           "c.isPublished = true AND " +
           "c.startDate >= :startFrom AND " +
           "(:pinCode IS NULL OR c.pinCode LIKE CONCAT(:pinCode, '%')) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:mode IS NULL OR c.mode = :mode) AND " +
           "(:isFree IS NULL OR c.isFree = :isFree) AND " +
           "(:startTo IS NULL OR c.startDate <= :startTo) " +
           "ORDER BY c.startDate ASC")
    Page<Course> searchCourses(
            @Param("pinCode") String pinCode,
            @Param("category") CourseCategory category,
            @Param("mode") CourseMode mode,
            @Param("isFree") Boolean isFree,
            @Param("startFrom") LocalDate startFrom,
            @Param("startTo") LocalDate startTo,
            Pageable pageable);

    Page<Course> findByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
            CourseCategory category, LocalDate date, Pageable pageable);

    Page<Course> findByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            CourseMode mode, LocalDate date, Pageable pageable);

    Page<Course> findByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            Boolean isFree, LocalDate date, Pageable pageable);

    long countByProviderId(Long providerId);
}

