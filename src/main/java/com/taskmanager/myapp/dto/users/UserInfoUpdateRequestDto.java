package com.taskmanager.myapp.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoUpdateRequestDto {

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;
}
