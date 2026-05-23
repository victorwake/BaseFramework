package com.example.base_framework.service;

import com.example.base_framework.entity.Module;
import com.example.base_framework.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public List<Module> findAll() {
        return moduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Module findById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Módulo no encontrado"));
    }

    @Transactional
    public Module create(String name) {
        if (moduleRepository.existsByName(name)) {
            throw new RuntimeException("Ya existe un módulo con ese nombre");
        }
        return moduleRepository.save(Module.builder().name(name).build());
    }

    @Transactional
    public Module update(Long id, String name) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Módulo no encontrado"));

        if (!module.getName().equals(name) && moduleRepository.existsByName(name)) {
            throw new RuntimeException("Ya existe un módulo con ese nombre");
        }

        module.setName(name);
        return moduleRepository.save(module);
    }

    @Transactional
    public void delete(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new RuntimeException("Módulo no encontrado");
        }
        moduleRepository.deleteById(id);
    }
}
