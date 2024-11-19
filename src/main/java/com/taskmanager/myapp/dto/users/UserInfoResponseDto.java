package com.taskmanager.myapp.dto.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponseDto {

    private String employeeNumber;
    private String username;
    private String phoneNumber;
    private String departmentName;
    private String roleName;

}
