package com.taskmanager.myapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.config.jwt.JwtTokenUtil;
import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.etc.LoginRequestDto;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @BeforeEach
    void setUp() {
        Roles role = rolesRepository.save(Roles.createRoles("사장"));
        Departments depart = departmentsRepository.save(Departments.createDepartments("개발1팀"));

        Users user = Users.builder()
                .id(0L)
                .role(role)
                .department(depart)
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password(passwordEncoder.encode("password"))
                .build();

        usersRepository.save(user);
    }

    @Test
    void 로그인_테스트() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();

        loginRequestDto.setEmployeeNumber("1234");
        loginRequestDto.setPassword("password");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        String savedRefreshToken = redisTemplate.opsForValue().get("1234");

        Assertions.assertNotNull(savedRefreshToken);
    }

    @Test
    void 로그인_실패_테스트_비밀번호_오류() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();

        loginRequestDto.setEmployeeNumber("1234");
        loginRequestDto.setPassword("password1234");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_실패_테스트_사원번호_오류() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();

        loginRequestDto.setEmployeeNumber("123456");
        loginRequestDto.setPassword("password");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 토큰_재발급_테스트() throws Exception {
        String refreshToken = jwtTokenUtil.generateRefreshToken("1234");
        redisTemplate.opsForValue().set("1234", refreshToken, jwtTokenUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        mockMvc.perform(post("/api/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andDo(print());
    }

    @Test
    void 토큰_재발급_실패_테스트() throws Exception {
        String refreshToken = jwtTokenUtil.generateRefreshToken("1234");

        mockMvc.perform(post("/api/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void 로그아웃_테스트() throws Exception {
        String accessToken = jwtTokenUtil.generateAccessToken("1234");

        mockMvc.perform(post("/api/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());

        String hashToken = jwtTokenUtil.tokenToHash(accessToken);

        String storedAccessToken = redisTemplate.opsForValue().get(hashToken);

        Assertions.assertNotNull(storedAccessToken);
        Assertions.assertEquals("blacklisted", storedAccessToken);
    }

}
