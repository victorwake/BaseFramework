package com.example.base_framework.repository;

import com.example.base_framework.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    List<Role> findByNameIn(List<String> names);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
}