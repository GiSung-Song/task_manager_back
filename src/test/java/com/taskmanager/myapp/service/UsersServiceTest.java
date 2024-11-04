package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.dto.UserRegisterRequestDto;
import com.taskmanager.myapp.exception.DataConflictException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @InjectMocks
    private UsersService usersService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private DepartmentsRepository departmentsRepository;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void 회원가입_성공_테스트() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");
        dto.setUsername("테스터");
        dto.setDepartmentId(0L);
        dto.setRoleId(0L);
        dto.setPhoneNumber("01012341234");

        Departments departments = Departments.createDepartments("테스트 부서");
        Roles roles = Roles.createRoles("사원");

        given(departmentsRepository.findById(0L)).willReturn(Optional.of(departments));
        given(rolesRepository.findById(0L)).willReturn(Optional.of(roles));

        usersService.registerUser(dto);

        verify(usersRepository, times(1)).save(argThat(user ->
                user.getEmployeeNumber().equals(dto.getEmployeeNumber()) &&
                user.getUsername().equals(dto.getUsername()) &&
                user.getDepartment().equals(departments) &&
                user.getRole().equals(roles)
        ));
    }

    @Test
    @DisplayName("회원가입 실패 테스트")
    void 회원가입_실패_테스트() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");
        dto.setUsername("테스터");
        dto.setDepartmentId(0L);
        dto.setRoleId(0L);
        dto.setPhoneNumber("01012341234");

        assertThrows(ResourceNotfoundException.class, () -> usersService.registerUser(dto));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 등록된 사원")
    void 회원가입_실패_테스트_등록된_사원() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");
        dto.setUsername("테스터");
        dto.setDepartmentId(0L);
        dto.setRoleId(0L);
        dto.setPhoneNumber("01012341234");

        Departments departments = Departments.createDepartments("테스트 부서");
        Roles roles = Roles.createRoles("사원");

        given(departmentsRepository.findById(0L)).willReturn(Optional.of(departments));
        given(rolesRepository.findById(0L)).willReturn(Optional.of(roles));
        given(usersRepository.existsByEmployeeNumber(dto.getEmployeeNumber())).willReturn(true);

        assertThrows(DataConflictException.class, () -> usersService.registerUser(dto));
    }
}