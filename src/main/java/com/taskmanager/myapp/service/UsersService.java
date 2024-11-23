package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.users.UserInfoResponseDto;
import com.taskmanager.myapp.dto.users.UserInfoUpdateRequestDto;
import com.taskmanager.myapp.dto.users.UserPasswordRequestDto;
import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.exception.CustomBadRequestException;
import com.taskmanager.myapp.exception.CustomDeniedException;
import com.taskmanager.myapp.exception.DataConflictException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final DepartmentsRepository departmentsRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    // 회원 정보 조회
    public UserInfoResponseDto getUserInfo(String employeeNumber) {
        Users user = securityService.getLoginUser();
        Users findUser = usersRepository.findByEmployeeNumber(employeeNumber);

        if (findUser == null) {
            throw new CustomBadRequestException("Invalid Employee Number");
        }

        // 1. 본인 정보 조회
        if (user.getId().equals(findUser.getId())) {
            UserInfoResponseDto dto = entityToDto(user);
            dto.setLoginUserDepartment(user.getDepartment().getDepartmentName());
            dto.setLoginUserLevel(user.getRole().getLevel());

            return dto;
        }

        // 2. 인사팀의 경우 모든 정보 조회
        if (user.getDepartment().getDepartmentName().startsWith("HR")) {
            UserInfoResponseDto dto = entityToDto(findUser);
            dto.setLoginUserDepartment(user.getDepartment().getDepartmentName());
            dto.setLoginUserLevel(user.getRole().getLevel());

            return dto;
        }

        // 예외처리 throw
        throw new CustomDeniedException("Invalid Access");
    }

    // 회원 정보 수정 - 휴대폰 번호
    @Transactional
    public void updatePhoneNumber(String employeeNumber, UserInfoUpdateRequestDto dto) {
        Users user = securityService.getLoginUser();
        Users findUser = usersRepository.findByEmployeeNumber(employeeNumber);

        if (findUser == null) {
            throw new CustomBadRequestException("Invalid Employee Number");
        }

        // 1. 본인이면 수정 가능
        if (user.getId().equals(findUser.getId())) {
            user.updatePhoneNumber(dto.getPhoneNumber());

            return;
        }

        // 2. 인사팀의 경우 Level4 직급 이상이면 모든 회원의 정보 수정 가능
        if (user.getDepartment().getDepartmentName().startsWith("HR")) {
            if (user.getRole().getLevel() >= 4) {
                findUser.updatePhoneNumber(dto.getPhoneNumber());

                return;
            }
        }

        throw new CustomDeniedException("Invalid Access");
    }

    // 회원 비밀번호 수정
    @Transactional
    public void updatePassword(String employeeNumber, UserPasswordRequestDto dto) {
        Users user = securityService.getLoginUser();
        Users findUser = usersRepository.findByEmployeeNumber(employeeNumber);

        if (findUser == null) {
            throw new CustomBadRequestException("Invalid Employee Number");
        }

        // 1. 본인이면 수정 가능
        if (user.getId().equals(findUser.getId())) {
            user.updatePassword(passwordEncoder.encode(dto.getPassword()));

            return;
        }

        // 예외처리 throw
        throw new CustomDeniedException("Invalid Access");
    }

    // 회원 비밀번호 초기화
    @Transactional
    public String resetPassword(String employeeNumber) {
        Users user = securityService.getLoginUser();
        Users findUser = usersRepository.findByEmployeeNumber(employeeNumber);

        if (findUser == null) {
            throw new CustomBadRequestException("Invalid Employee Number");
        }

        // 인사팀의 경우 Level4 직급 이상일 때 비밀번호 초기화 가능
        if (user.getDepartment().getDepartmentName().startsWith("HR")) {
            if (user.getRole().getLevel() >= 4) {
                String password = randomPassword();
                findUser.updatePassword(passwordEncoder.encode(password));

                return password;
            }
        }

        throw new CustomDeniedException("Invalid Access");
    }

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

    private UserInfoResponseDto entityToDto(Users user) {
        UserInfoResponseDto dto = new UserInfoResponseDto();

        dto.setUsername(user.getUsername());
        dto.setEmployeeNumber(user.getEmployeeNumber());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDepartmentName(user.getDepartment().getDepartmentName());
        dto.setRoleName(user.getRole().getRoleName());
        return dto;
    }

    private String randomPassword() {
        String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?/";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(character.length());
            password.append(character.charAt(index));
        }

        return password.toString();
    }
}
