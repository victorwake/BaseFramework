package com.example.base_framework.repository;

import com.example.base_framework.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByName(String name);
    boolean existsByName(String name);
}
