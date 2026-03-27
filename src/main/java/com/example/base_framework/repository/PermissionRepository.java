package com.example.base_framework.repository;

import com.example.base_framework.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
    List<Permission> findAllByNameIn(List<String> names);

    List<Permission> findByNameIn(List<String> names);
}