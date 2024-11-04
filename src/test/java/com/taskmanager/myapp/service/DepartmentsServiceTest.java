package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.dto.DepartmentsDto;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepartmentsServiceTest {

    @InjectMocks
    private DepartmentsService departmentsService;

    @Mock
    private DepartmentsRepository departmentsRepository;

    @Test
    @DisplayName("전체 부서 조회")
    void 전체_부서_조회() {
        Departments departments1 = Departments.createDepartments("개발 1팀");
        Departments departments2 = Departments.createDepartments("개발 2팀");
        Departments departments3 = Departments.createDepartments("개발 3팀");

        List<Departments> departmentsList = List.of(departments1, departments2, departments3);

        given(departmentsRepository.findAll()).willReturn(departmentsList);

        List<DepartmentsDto> allDepartments = departmentsService.getAllDepartments();

        assertEquals(3, allDepartments.size());
        assertEquals("개발 1팀", allDepartments.get(0).getDepartmentName());
    }

}