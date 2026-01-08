package com.task.edtech.db.repository;

import com.task.edtech.db.entity.Course;
import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
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

    @Query("SELECT c FROM Course c WHERE c.user.id = :userId")
    List<Course> findByUser_Id(@Param("userId") Long userId);

    @Query("SELECT c FROM Course c WHERE c.user.id = :userId")
    Page<Course> findByUser_Id(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.pinCode LIKE CONCAT(:pinPrefix, '%') " +
           "AND c.isPublished = true AND c.startDate >= :date")
    List<Course> findByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @Param("pinPrefix") String pinPrefix, @Param("date") LocalDate date);

    @Query("SELECT c FROM Course c WHERE c.pinCode = :pinCode " +
           "AND c.isPublished = true AND c.startDate >= :date")
    List<Course> findByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @Param("pinCode") String pinCode, @Param("date") LocalDate date);

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

    @Query("SELECT c FROM Course c WHERE c.category = :category " +
           "AND c.isPublished = true AND c.startDate >= :date")
    Page<Course> findByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @Param("category") CourseCategory category, 
            @Param("date") LocalDate date, 
            Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.mode = :mode " +
           "AND c.isPublished = true AND c.startDate >= :date")
    Page<Course> findByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @Param("mode") CourseMode mode, 
            @Param("date") LocalDate date, 
            Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isFree = :isFree " +
           "AND c.isPublished = true AND c.startDate >= :date")
    Page<Course> findByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            @Param("isFree") Boolean isFree, 
            @Param("date") LocalDate date, 
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.user.id = :userId")
    long countByUser_Id(@Param("userId") Long userId);
}

