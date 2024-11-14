package com.taskmanager.myapp.dto.etc;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
