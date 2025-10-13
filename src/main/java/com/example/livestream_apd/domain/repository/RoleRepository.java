package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(Role.RoleName name);

    boolean existsByName(Role.RoleName name);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") Role.RoleName name);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.id IN :ids")
    Set<Role> findByIdInWithPermissions(@Param("ids") Set<Long> ids);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions")
    Set<Role> findAllWithPermissions();
}
