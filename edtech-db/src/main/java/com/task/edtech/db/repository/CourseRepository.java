package com.task.edtech.db.repository;

import com.task.edtech.db.entity.Course;
import com.task.edtech.db.enums.CourseCategory;
import com.task.edtech.db.enums.CourseMode;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findById(@NotNull @Param("id") Long id);

    @Query("SELECT c FROM Course c WHERE c.internalId = :internalId")
    Optional<Course> findByInternalId(@NotNull @Param("internalId") UUID internalId);

    @Query("SELECT c FROM Course c WHERE c.user.id = :userId AND c.title = :title")
    Optional<Course> findByUserIdAndTitle(
            @NotNull @Param("userId") Long userId,
            @NotNull @Param("title") String title);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Course c WHERE c.user.id = :userId")
    List<Course> getAllByUserId(@NotNull @Param("userId") Long userId);

    @Query("SELECT c FROM Course c WHERE " +
            "c.isPublished = true AND " +
            "c.startDate >= :startFrom AND " +
            "(:pinCodePattern IS NULL OR (c.pinCode IS NOT NULL AND c.pinCode LIKE :pinCodePattern)) AND " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:mode IS NULL OR c.mode = :mode) AND " +
            "(:isFree IS NULL OR c.isFree = :isFree) AND " +
            "(:startTo IS NULL OR c.startDate <= :startTo) " +
            "ORDER BY c.startDate ASC")
    List<Course> searchCourses(
            @Param("pinCodePattern") String pinCodePattern,
            @Param("category") CourseCategory category,
            @Param("mode") CourseMode mode,
            @Param("isFree") Boolean isFree,
            @Param("startFrom") LocalDate startFrom,
            @Param("startTo") LocalDate startTo);
}

