package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.users.UserInfoResponseDto;
import com.taskmanager.myapp.dto.users.UserInfoUpdateRequestDto;
import com.taskmanager.myapp.dto.users.UserPasswordRequestDto;
import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.exception.CustomBadRequestException;
import com.taskmanager.myapp.exception.CustomDeniedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Mock
    private SecurityService securityService;

    @Mock
    private PasswordEncoder passwordEncoder;

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
        Roles roles = Roles.createRoles("사원", 1);

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
        Roles roles = Roles.createRoles("사원", 1);

        given(departmentsRepository.findById(0L)).willReturn(Optional.of(departments));
        given(rolesRepository.findById(0L)).willReturn(Optional.of(roles));
        given(usersRepository.existsByEmployeeNumber(dto.getEmployeeNumber())).willReturn(true);

        assertThrows(DataConflictException.class, () -> usersService.registerUser(dto));
    }

    @Test
    void 회원정보_조회_개인() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);

        UserInfoResponseDto userInfo = usersService.getUserInfo("1234");

        assertEquals("테스터", userInfo.getUsername());
        assertEquals("부장", userInfo.getRoleName());
        assertEquals("개발1팀", userInfo.getDepartmentName());
    }

    @Test
    void 회원정보_조회_인사() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        Users user2 = Users.builder()
                .id(1L)
                .role(Roles.createRoles("사원", 1))
                .department(Departments.createDepartments("개발3팀"))
                .username("테스퉁")
                .phoneNumber("12341234")
                .employeeNumber("121212")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user2);

        UserInfoResponseDto userInfo = usersService.getUserInfo("121212");

        assertEquals("테스퉁", userInfo.getUsername());
        assertEquals("사원", userInfo.getRoleName());
        assertEquals("개발3팀", userInfo.getDepartmentName());
    }

    @Test
    void 회원정보_조회_실패() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber("1234")).thenReturn(null);

        assertThrows(CustomBadRequestException.class, () -> usersService.getUserInfo("1234"));
    }

    @Test
    void 회원정보_조회_실패2() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        Users user2 = Users.builder()
                .id(1L)
                .role(Roles.createRoles("사원", 1))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스퉁")
                .phoneNumber("1234567812")
                .employeeNumber("121234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber("121234")).thenReturn(user2);

        assertThrows(CustomDeniedException.class, () -> usersService.getUserInfo("121234"));
    }

    @Test
    void 회원정보_수정_개인() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();
        dto.setPhoneNumber("01012341234");

        usersService.updatePhoneNumber("1234", dto);

        assertEquals("01012341234", user.getPhoneNumber());
    }

    @Test
    void 회원정보_수정_인사() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        Users user2 = Users.builder()
                .id(1L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스퉁")
                .phoneNumber("1234567812")
                .employeeNumber("121234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user2);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();
        dto.setPhoneNumber("01012341234");

        usersService.updatePhoneNumber("1234", dto);

        assertEquals("01012341234", user.getPhoneNumber());
    }

    @Test
    void 회원정보_수정_실패() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber("1234")).thenReturn(null);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();
        dto.setPhoneNumber("01012341234");

        assertThrows(CustomBadRequestException.class, () -> usersService.updatePhoneNumber("1234", dto));
    }

    @Test
    void 회원정보_수정_실패2() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        Users user2 = Users.builder()
                .id(1L)
                .role(Roles.createRoles("사원", 1))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스퉁")
                .phoneNumber("1234567812")
                .employeeNumber("121234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber("121234")).thenReturn(user2);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();
        dto.setPhoneNumber("01012341234");

        assertThrows(CustomDeniedException.class, () -> usersService.updatePhoneNumber("121234", dto));
    }

    @Test
    void 회원_비밀번호_수정() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();
        dto.setPassword("12341234");

        usersService.updatePassword("1234", dto);

        assertEquals(passwordEncoder.encode("12341234"), user.getPassword());
    }

    @Test
    void 회원_비밀번호_수정_실패() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(null);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();
        dto.setPassword("12341234");

        assertThrows(CustomBadRequestException.class, () -> usersService.updatePassword("1234", dto));
    }

    @Test
    void 회원_비밀번호_초기화() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);

        String resetPassword = usersService.resetPassword("1234");

        assertEquals(passwordEncoder.encode(resetPassword), user.getPassword());
    }

    @Test
    void 회원_비밀번호_초기화_실패() {
        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        when(securityService.getLoginUser()).thenReturn(user);
        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(null);

        assertThrows(CustomBadRequestException.class, () -> usersService.resetPassword("1234"));
    }
}