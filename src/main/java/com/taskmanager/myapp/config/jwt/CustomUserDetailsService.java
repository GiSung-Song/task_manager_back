package com.taskmanager.myapp.config.jwt;

import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeNumber) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmployeeNumber(employeeNumber);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with employee number : " + employeeNumber);
        }

        return User.builder()
                .username(user.getEmployeeNumber())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();
    }
}
