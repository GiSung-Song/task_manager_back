package com.taskmanager.myapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.dto.users.UserInfoResponseDto;
import com.taskmanager.myapp.dto.users.UserInfoUpdateRequestDto;
import com.taskmanager.myapp.dto.users.UserPasswordRequestDto;
import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.service.UsersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = UsersController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OncePerRequestFilter.class))
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @InjectMocks
    private UsersController usersController;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void 회원가입_성공_테스트() throws Exception {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");
        dto.setUsername("테스터");
        dto.setDepartmentId(0L);
        dto.setRoleId(0L);
        dto.setPhoneNumber("01012341234");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(usersService, times(1)).registerUser(any(UserRegisterRequestDto.class));
    }

    @Test
    @DisplayName("회원가입 실패 테스트")
    void 회원가입_실패_테스트() throws Exception {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");
        dto.setUsername("테스터");
        dto.setDepartmentId(0L);
        dto.setRoleId(0L);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원정보_조회() throws Exception {
        UserInfoResponseDto dto = new UserInfoResponseDto();

        dto.setUsername("테스터");
        dto.setDepartmentName("개발1팀");
        dto.setRoleName("대리");
        dto.setPhoneNumber("123412341234");
        dto.setEmployeeNumber("test-1234");

        when(usersService.getUserInfo(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/users/{employeeNumber}", "test-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("테스터"))
                .andExpect(jsonPath("$.data.departmentName").value("개발1팀"))
                .andDo(print());
    }

    @Test
    void 회원정보_수정() throws Exception {
        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        dto.setPhoneNumber("01012341234");

        mockMvc.perform(patch("/api/users/{employeeNumber}", "test-1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 회원정보_수정_실패() throws Exception {
        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        mockMvc.perform(patch("/api/users/{employeeNumber}", "test-1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_수정() throws Exception {
        UserPasswordRequestDto dto = new UserPasswordRequestDto();

        dto.setPassword("01012341234");

        mockMvc.perform(patch("/api/users/{employeeNumber}/password", "test-1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_수정_실패() throws Exception {
        UserPasswordRequestDto dto = new UserPasswordRequestDto();

        mockMvc.perform(patch("/api/users/{employeeNumber}/password", "test-1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_초기화() throws Exception {
        mockMvc.perform(post("/api/users/{employeeNumber}/reset", "test-1234"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}