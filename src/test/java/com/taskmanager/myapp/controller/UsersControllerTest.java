package com.taskmanager.myapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.dto.UserRegisterRequestDto;
import com.taskmanager.myapp.service.UsersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UsersController.class)
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

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."))
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

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}