package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.auth.LoginRequestDto;
import com.taskmanager.myapp.dto.auth.TokenDto;
import com.taskmanager.myapp.exception.CustomAuthException;
import com.taskmanager.myapp.global.CustomResponse;
import com.taskmanager.myapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CustomResponse<TokenDto>> login(@RequestBody @Valid LoginRequestDto dto, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(dto);

        Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); //2주

        response.addCookie(cookie);

        return ResponseEntity.ok(CustomResponse.res(tokenDto, "Login Success"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CustomResponse<TokenDto>> reIssueAccessToken(@CookieValue("refreshToken") String refreshToken) {
        TokenDto tokenDto = authService.reIssueAccessToken(refreshToken);

        return ResponseEntity.ok(CustomResponse.res(tokenDto, "Refresh Access Token Success"));
    }

    @PostMapping("/logout")
    public ResponseEntity<CustomResponse<String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomAuthException("Invalid Authorization");
        }

        String accessToken = authorizationHeader.substring(7);

        authService.logout(accessToken);

        return ResponseEntity.ok(CustomResponse.res(null, "Logout Success"));
    }
}