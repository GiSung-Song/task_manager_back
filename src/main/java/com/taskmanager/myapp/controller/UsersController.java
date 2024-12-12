package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.users.*;
import com.taskmanager.myapp.global.CustomResponse;
import com.taskmanager.myapp.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UsersController {

    private final UsersService usersService;

    // 회원 조회
    @GetMapping("/users/{employeeNumber}")
    public ResponseEntity<CustomResponse<UserInfoResponseDto>> getUserInfo(@PathVariable String employeeNumber) {
        UserInfoResponseDto userInfo = usersService.getUserInfo(employeeNumber);

        return ResponseEntity.ok(CustomResponse.res(userInfo, "회원정보를 조회했습니다."));
    }

    // 회원 정보 수정 - 휴대폰 번호
    @PatchMapping("/users/{employeeNumber}")
    public ResponseEntity<CustomResponse<String>> updatePhoneNumber(@PathVariable String employeeNumber, @RequestBody @Valid UserInfoUpdateRequestDto dto) {
        usersService.updatePhoneNumber(employeeNumber, dto);

        return ResponseEntity.ok(CustomResponse.res(null, "회원정보를 수정했습니다."));
    }

    // 회원 비밀번호 수정
    @PatchMapping("/users/{employeeNumber}/password")
    public ResponseEntity<CustomResponse<String>> updatePassword(@PathVariable String employeeNumber, @RequestBody @Valid UserPasswordRequestDto dto) {
        usersService.updatePassword(employeeNumber, dto);

        return ResponseEntity.ok(CustomResponse.res(null, "비밀번호를 수정했습니다."));
    }

    // 회원 비밀번호 초기화
    @PostMapping("/users/{employeeNumber}/reset")
    public ResponseEntity<CustomResponse<UserResetPasswordResponseDto>> resetPassword(@PathVariable String employeeNumber) {
        UserResetPasswordResponseDto dto = usersService.resetPassword(employeeNumber);

        return ResponseEntity.ok(CustomResponse.res(dto, "비밀번호를 초기화했습니다."));
    }

    @PostMapping("/users")
    public ResponseEntity<CustomResponse<String>> registerUser(@RequestBody @Valid UserRegisterRequestDto dto) {
        usersService.registerUser(dto);

        return ResponseEntity.ok(CustomResponse.res(null, "회원가입이 완료되었습니다."));
    }

}
