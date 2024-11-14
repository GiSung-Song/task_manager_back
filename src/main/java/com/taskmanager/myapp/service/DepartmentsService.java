package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.dto.departments.DepartmentsDto;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentsService {

    private final DepartmentsRepository departmentsRepository;

    @Transactional(readOnly = true)
    public List<DepartmentsDto> getAllDepartments() {
        return departmentsRepository.findAll().stream()
                .map(entity -> toDto(entity))
                .collect(Collectors.toList());
    }

    private DepartmentsDto toDto(Departments departments) {
        return new DepartmentsDto(departments.getId(), departments.getDepartmentName());
    }

}
