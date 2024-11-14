package com.taskmanager.myapp.config.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    private String secret = "fdasjoifdjaso32poujf3802h0fdaf32j10if0jh12f3hi231fh";
    private Long accessTokenExpiration = 1000 * 5L; // 5초
    private Long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7L; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil(secret, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    void access_token_생성_테스트() {
        String employeNumber = "12345";
        String accessToken = jwtTokenUtil.generateAccessToken(employeNumber);

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