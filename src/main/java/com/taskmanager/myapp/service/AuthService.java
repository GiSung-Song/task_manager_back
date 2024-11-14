package com.taskmanager.myapp.service;

import com.taskmanager.myapp.config.jwt.JwtTokenUtil;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.etc.LoginRequestDto;
import com.taskmanager.myapp.dto.etc.TokenDto;
import com.taskmanager.myapp.exception.CustomAuthException;
import com.taskmanager.myapp.exception.CustomBadRequestException;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenDto login(LoginRequestDto dto) {
        Users user = usersRepository.findByEmployeeNumber(dto.getEmployeeNumber());

        if (user == null) {
            throw new CustomBadRequestException("Invalid employee number");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomBadRequestException("Invalid password");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(dto.getEmployeeNumber());
        String refreshToken = jwtTokenUtil.generateRefreshToken(dto.getEmployeeNumber());

        redisTemplate.opsForValue().set(dto.getEmployeeNumber(), refreshToken, jwtTokenUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        return new TokenDto(accessToken, refreshToken);
    }

    public TokenDto reIssueAccessToken(String refreshToken) {
        String employeeNumber = jwtTokenUtil.extractEmployeeNumber(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get(employeeNumber);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new CustomAuthException("Invalid Refresh Token");
        }

        String newAccessToken = jwtTokenUtil.generateAccessToken(employeeNumber);

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);

        return tokenDto;
    }

    public void logout(String accessToken) {
        Date expiration = jwtTokenUtil.getExpiration(accessToken);

        if (expiration != null && expiration.after(new Date())) {
            long expirationTime = expiration.getTime() - System.currentTimeMillis();
            String hashAccessToken = jwtTokenUtil.tokenToHash(accessToken);

            redisTemplate.opsForValue().set(hashAccessToken, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
        } else {
            throw new CustomAuthException("Invalid Access Token For Logout");
        }
    }
}
