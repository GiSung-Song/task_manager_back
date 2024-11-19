package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UsersRepository usersRepository;

    public Users getLoginUser() {
        String employeeNumber = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = usersRepository.findByEmployeeNumber(employeeNumber);

        if (user == null) {
            throw new ResourceNotfoundException("Invalid User");
        }

        return user;
    }

}
