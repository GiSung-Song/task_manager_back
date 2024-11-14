package com.taskmanager.myapp.config.aop;

import com.taskmanager.myapp.domain.Tasks;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.exception.CustomAuthException;
import com.taskmanager.myapp.exception.CustomBadRequestException;
import com.taskmanager.myapp.exception.CustomDeniedException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.TasksRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TaskOwnerAspect {

    private final TasksRepository tasksRepository;
    private final UsersRepository usersRepository;

    @Around("@annotation(com.taskmanager.myapp.global.TaskOwnerCheck) && args(taskId, ..)")
    public Object checkTaskOwner(ProceedingJoinPoint proceedingJoinPoint, Long taskId) throws Throwable {
        String employeeNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findByEmployeeNumber(employeeNumber);
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new CustomBadRequestException("Invalid Task ID"));

        if (user == null) {
            throw new ResourceNotfoundException("Invalid User");
        }

        if (task.getUser().getId() != user.getId()) {
            throw new CustomDeniedException("Invalid Task Owner");
        }

        return proceedingJoinPoint.proceed();
    }
}
