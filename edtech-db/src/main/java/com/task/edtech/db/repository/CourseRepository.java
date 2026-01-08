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
    
    /**
     * Find all courses by provider ID
     * @param providerId the provider ID
     * @return list of courses for the provider
     */
    List<Course> findByProviderId(Long providerId);
    
    /**
     * Find courses by provider ID with pagination
     * @param providerId the provider ID
     * @param pageable pagination information
     * @return page of courses for the provider
     */
    Page<Course> findByProviderId(Long providerId, Pageable pageable);
    
    /**
     * Find courses by PIN code starting with prefix, published, and start date greater than or equal to given date
     * Used for "nearby" PIN code matching (first 3 digits match)
     * @param pinPrefix the PIN code prefix (first 3 digits)
     * @param date the minimum start date
     * @return list of matching courses
     */
    List<Course> findByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(
            String pinPrefix, LocalDate date);
    
    /**
     * Find courses by exact PIN code, published, and start date greater than or equal to given date
     * @param pinCode the exact PIN code
     * @param date the minimum start date
     * @return list of matching courses
     */
    List<Course> findByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            String pinCode, LocalDate date);
    
    /**
     * Search courses with multiple filters
     * Finds published courses with future start dates matching the criteria
     * @param pinCode the PIN code (exact or prefix)
     * @param category the course category (optional, can be null)
     * @param mode the course mode (optional, can be null)
     * @param isFree whether the course is free (optional, can be null)
     * @param startFrom minimum start date
     * @param startTo maximum start date (optional, can be null)
     * @param pageable pagination information
     * @return page of matching courses
     */
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
    
    /**
     * Find published courses by category
     * @param category the course category
     * @param date the minimum start date
     * @param pageable pagination information
     * @return page of matching courses
     */
    Page<Course> findByCategoryAndIsPublishedTrueAndStartDateGreaterThanEqual(
            CourseCategory category, LocalDate date, Pageable pageable);
    
    /**
     * Find published courses by mode
     * @param mode the course mode
     * @param date the minimum start date
     * @param pageable pagination information
     * @return page of matching courses
     */
    Page<Course> findByModeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            CourseMode mode, LocalDate date, Pageable pageable);
    
    /**
     * Find published free courses
     * @param isFree whether the course is free
     * @param date the minimum start date
     * @param pageable pagination information
     * @return page of matching courses
     */
    Page<Course> findByIsFreeAndIsPublishedTrueAndStartDateGreaterThanEqual(
            Boolean isFree, LocalDate date, Pageable pageable);
    
    /**
     * Count courses by provider ID
     * @param providerId the provider ID
     * @return count of courses for the provider
     */
    long countByProviderId(Long providerId);
}

