package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.global.CustomResponse;
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
@RequestMapping("/api")
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/users")
    public ResponseEntity<CustomResponse<String>> registerUser(@RequestBody @Valid UserRegisterRequestDto dto) {
        usersService.registerUser(dto);

        return ResponseEntity.ok(CustomResponse.res(null, "회원가입이 완료되었습니다."));
    }

}
