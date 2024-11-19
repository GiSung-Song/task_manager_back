package com.taskmanager.myapp.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPasswordRequestDto {

    @NotBlank(message = "Password is required")
    private String password;

}
