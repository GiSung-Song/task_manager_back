package com.taskmanager.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegisterRequestDto {

    @NotBlank(message = "Employee Number is required")
    private String employeeNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Rank is required")
    private Long roleId;

}
