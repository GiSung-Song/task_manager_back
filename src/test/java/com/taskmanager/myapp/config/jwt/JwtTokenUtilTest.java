package com.taskmanager.myapp.config.jwt;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    private String secret = "fdasjoifdjaso32poujf3802h0fdaf32j10if0jh12f3hi231fh";
    private Long accessTokenExpiration = 1000 * 5L; // 5초
    private Long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7L; // 7일

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil(secret, accessTokenExpiration, refreshTokenExpiration, usersRepository);

        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("HR1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("1234")
                .password("password")
                .build();

        when(usersRepository.findByEmployeeNumber(anyString())).thenReturn(user);
    }

    @Test
    void access_token_생성_테스트() {
        String employeeNumber = "12345";
        String accessToken = jwtTokenUtil.generateAccessToken(employeeNumber);

        assertNotNull(accessToken);
        assertTrue(accessToken.startsWith("ey"));
    }

    @Test
    void employee_number_추출_테스트() {
        String employeeNumber = "12345";
        String accessToken = jwtTokenUtil.generateAccessToken(employeeNumber);
        String extractEmployeeNumber = jwtTokenUtil.extractEmployeeNumber(accessToken);

        assertEquals(employeeNumber, extractEmployeeNumber);
    }

    @Test
    void token_유효성_체크_테스트() {
        String employeeNumber = "12345";
        String accessToken = jwtTokenUtil.generateAccessToken(employeeNumber);

        assertTrue(jwtTokenUtil.validateToken(accessToken, employeeNumber));
    }

    @Test
    void token_만료_체크_테스트() {
        String employeeNumber = "12345";
        String accessToken = jwtTokenUtil.generateAccessToken(employeeNumber);

        try {
            Thread.sleep(accessTokenExpiration + 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse(jwtTokenUtil.validateToken(accessToken, employeeNumber));
    }

}