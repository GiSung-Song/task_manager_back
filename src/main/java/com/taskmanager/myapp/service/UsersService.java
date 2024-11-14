package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.exception.DataConflictException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final DepartmentsRepository departmentsRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserRegisterRequestDto dto) {
        Departments department = departmentsRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotfoundException("Department is not exist"));

        Roles role = rolesRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotfoundException("Role is not exist"));

        if (usersRepository.existsByEmployeeNumber(dto.getEmployeeNumber())) {
            throw new DataConflictException("Employee Number is already registered");
        }

        Users user = Users.builder()
                .employeeNumber(dto.getEmployeeNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .username(dto.getUsername())
                .phoneNumber(dto.getPhoneNumber())
                .department(department)
                .role(role)
                .build();

        usersRepository.save(user);
    }
}
