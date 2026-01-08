package com.task.edtech.db.repository;

import com.task.edtech.db.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    
    /**
     * Find provider by email address
     * @param email the email address to search for
     * @return Optional containing the provider if found
     */
    Optional<Provider> findByEmail(String email);
    
    /**
     * Check if a provider exists with the given email
     * @param email the email address to check
     * @return true if provider exists, false otherwise
     */
    boolean existsByEmail(String email);
}

