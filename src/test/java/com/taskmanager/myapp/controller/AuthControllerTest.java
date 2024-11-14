package com.taskmanager.myapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.dto.etc.LoginRequestDto;
import com.taskmanager.myapp.dto.etc.TokenDto;
import com.taskmanager.myapp.exception.CustomAuthException;
import com.taskmanager.myapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OncePerRequestFilter.class))
class AuthControllerTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 로그인_테스트() throws Exception {
        LoginRequestDto dto = new LoginRequestDto();

        dto.setEmployeeNumber("test-1234");
        dto.setPassword("12341234");

        TokenDto tokenDto = new TokenDto("accessToken", "refreshToken");

        when(authService.login(any())).thenReturn(tokenDto);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 60 * 60 * 24 * 14))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andDo(print());

        verify(authService, times(1)).login(any());
    }

    @Test
    void 토큰_재발급_테스트() throws Exception {
        String refreshToken = "refreshToken";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken("accessToken");

        when(authService.reIssueAccessToken(refreshToken)).thenReturn(tokenDto);

        mockMvc.perform(post("/api/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andDo(print());

        verify(authService, times(1)).reIssueAccessToken(refreshToken);
    }

    @Test
    void 토큰_재발급_실패_테스트() throws Exception {
        String refreshToken = "refreshToken";

        when(authService.reIssueAccessToken(refreshToken)).thenThrow(new CustomAuthException("Invalid Refresh Token"));

        mockMvc.perform(post("/api/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andDo(print());

        verify(authService, times(1)).reIssueAccessToken(refreshToken);
    }

    @Test
    void 로그아웃_테스트() throws Exception {
        String accessToken = "accessToken";
        String authorizationHeader = "Bearer accessToken";

        doNothing().when(authService).logout(accessToken);

        mockMvc.perform(post("/api/logout")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andDo(print());

        verify(authService, times(1)).logout(accessToken);
    }

    @Test
    void 로그아웃_실패_테스트() throws Exception {
        String accessToken = "accessToken";
        String authorizationHeader = "BeareraccessToken";
        doNothing().when(authService).logout(accessToken);

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", authorizationHeader))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

}