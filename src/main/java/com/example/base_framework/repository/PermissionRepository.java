package com.example.base_framework.repository;

import com.example.base_framework.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
    List<Permission> findByNameIn(List<String> names);
    Optional<Permission> findByName(String name);
}