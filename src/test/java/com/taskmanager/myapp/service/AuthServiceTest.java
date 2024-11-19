package com.taskmanager.myapp.service;

import com.taskmanager.myapp.config.jwt.JwtTokenUtil;
import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.etc.LoginRequestDto;
import com.taskmanager.myapp.dto.etc.TokenDto;
import com.taskmanager.myapp.exception.CustomAuthException;
import com.taskmanager.myapp.exception.CustomBadRequestException;
import com.taskmanager.myapp.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void 로그인_성공_테스트() {
        String employeeNumber = "test-1234";
        String password = "12341234";
        String encodedPassword = "fdsjaip321ijpfds";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long tokenExpiration = 600000L;

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmployeeNumber(employeeNumber);
        loginRequestDto.setPassword(password);

        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber(employeeNumber)
                .password(encodedPassword)
                .build();

        when(usersRepository.findByEmployeeNumber(employeeNumber)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenUtil.generateAccessToken(employeeNumber)).thenReturn(accessToken);
        when(jwtTokenUtil.generateRefreshToken(employeeNumber)).thenReturn(refreshToken);
        when(jwtTokenUtil.getRefreshTokenExpiration()).thenReturn(tokenExpiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TokenDto tokenDto = authService.login(loginRequestDto);

        assertEquals(accessToken, tokenDto.getAccessToken());
        assertEquals(refreshToken, tokenDto.getRefreshToken());
        verify(redisTemplate.opsForValue(), times(1)).set(employeeNumber, refreshToken, tokenExpiration, TimeUnit.MILLISECONDS);
    }

    @Test
    void 로그인_실패_테스트_사번() {
        String employeeNumber = "test-1234";
        LoginRequestDto dto = new LoginRequestDto();

        dto.setEmployeeNumber(employeeNumber);
        dto.setPassword("12341234");

        when(usersRepository.findByEmployeeNumber(employeeNumber)).thenReturn(null);

        assertThrows(CustomBadRequestException.class, () -> authService.login(dto));
    }

    @Test
    void 로그인_실패_테스트_비밀번호() {
        String employeeNumber = "test-1234";
        String password = "12341234";
        String encodedPassword = "fdsjaip321ijpfds";

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmployeeNumber(employeeNumber);
        loginRequestDto.setPassword(password);

        Users user = Users.builder()
                .id(0L)
                .role(Roles.createRoles("부장", 4))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber(employeeNumber)
                .password(encodedPassword)
                .build();

        when(usersRepository.findByEmployeeNumber(employeeNumber)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(CustomBadRequestException.class, () -> authService.login(loginRequestDto));
    }

    @Test
    void 토큰_재발급_테스트() {
        String refreshToken = "validRefreshToken";
        String employeeNumber = "12345";
        String newAccessToken = "newAccessToken";

        when(jwtTokenUtil.extractEmployeeNumber(refreshToken)).thenReturn(employeeNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(employeeNumber)).thenReturn(refreshToken);
        when(jwtTokenUtil.generateAccessToken(employeeNumber)).thenReturn(newAccessToken);

        TokenDto tokenDto = authService.reIssueAccessToken(refreshToken);

        assertNotNull(tokenDto);
        assertEquals(newAccessToken, tokenDto.getAccessToken());
    }

    @Test
    void 토큰_재발급_실패_테스트() {
        String refreshToken = "validRefreshToken";
        String employeeNumber = "12345";

        when(jwtTokenUtil.extractEmployeeNumber(refreshToken)).thenReturn(employeeNumber);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(employeeNumber)).thenReturn(null);

        assertThrows(CustomAuthException.class, () -> authService.reIssueAccessToken(refreshToken));
    }

    @Test
    void 로그아웃_성공_테스트() {
        Date expiration = new Date(System.currentTimeMillis() + 60000);
        String accessToken = "accessToken";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtTokenUtil.getExpiration(accessToken)).thenReturn(expiration);

        authService.logout(accessToken);

        verify(valueOperations, times(1)).set(any(), eq("blacklisted"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void 로그아웃_실패_테스트() {
        String accessToken = "accessToken";

        when(jwtTokenUtil.getExpiration(accessToken)).thenReturn(null);

        assertThrows(CustomAuthException.class, () -> authService.logout(accessToken));
    }

}