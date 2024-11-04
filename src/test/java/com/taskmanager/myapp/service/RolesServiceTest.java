package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.dto.RolesDto;
import com.taskmanager.myapp.repository.RolesRepository;
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
class RolesServiceTest {

    @InjectMocks
    private RolesService rolesService;

    @Mock
    private RolesRepository rolesRepository;

    @Test
    @DisplayName("직급 전체 조회")
    void 직급_전체_조회() {
        Roles role1 = Roles.createRoles("사원");
        Roles role2 = Roles.createRoles("대리");
        Roles role3 = Roles.createRoles("과장");

        List<Roles> rolesList = List.of(role1, role2, role3);

        given(rolesRepository.findAll()).willReturn(rolesList);

        List<RolesDto> rolesDtoList = rolesService.getAllRoles();

        assertEquals(3, rolesDtoList.size());
        assertEquals("사원", rolesDtoList.get(0).getRoleName());
    }
}