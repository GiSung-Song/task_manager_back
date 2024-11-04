package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.dto.RolesDto;
import com.taskmanager.myapp.repository.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolesService {

    private final RolesRepository rolesRepository;

    @Transactional(readOnly = true)
    public List<RolesDto> getAllRoles() {
        return rolesRepository.findAll().stream()
                .map(entity -> toDto(entity))
                .collect(Collectors.toList());
    }

    private RolesDto toDto(Roles roles) {
        return new RolesDto(roles.getId(), roles.getRoleName());
    }
}
