package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.UserRegisterRequestDto;
import com.taskmanager.myapp.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserRegisterRequestDto dto) {
        usersService.registerUser(dto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

}
