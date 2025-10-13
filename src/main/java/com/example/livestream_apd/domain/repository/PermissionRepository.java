package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Set<Permission> findByNameIn(Set<String> names);

    Set<Permission> findByCategory(String category);

    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:pattern%")
    Set<Permission> findByNameContaining(@Param("pattern") String pattern);
}
