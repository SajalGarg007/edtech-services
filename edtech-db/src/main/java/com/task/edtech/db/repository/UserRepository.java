package com.task.edtech.db.repository;

import com.task.edtech.db.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findById(@NotNull @Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.internalId = :internalId")
    Optional<User> findByInternalId(@NotNull @Param("internalId") UUID internalId);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@NotNull @Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@NotNull @Param("email") String email);
}

