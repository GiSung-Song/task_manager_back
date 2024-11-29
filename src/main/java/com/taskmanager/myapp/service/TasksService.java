package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Tasks;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import com.taskmanager.myapp.dto.tasks.*;
import com.taskmanager.myapp.exception.CustomDeniedException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.global.TaskOwnerCheck;
import com.taskmanager.myapp.repository.TasksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TasksService {

    private final TasksRepository tasksRepository;
    private final SecurityService securityService;

    // 업무 생성
    @Transactional
    public void addTask(TaskRegisterRequestDto dto) {
        Users user = securityService.getLoginUser();

        TaskStatus taskStatus = dto.getTaskStatus() != null ? TaskStatus.valueOf(dto.getTaskStatus()) : TaskStatus.PENDING;
        TaskPriority priority = dto.getTaskPriority() != null ? TaskPriority.valueOf(dto.getTaskPriority()) : TaskPriority.MEDIUM;
        TaskType taskType = dto.getTaskType() != null ? TaskType.valueOf(dto.getTaskType()) : TaskType.PERSONAL;
        LocalDateTime startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now();
        LocalDateTime deadline = dto.getDeadline() != null ? dto.getDeadline() : LocalDateTime.now().plusDays(1);

        // Role Level 3이하면 부서 전체 업무 추가 불가능
        if (taskType == TaskType.TEAM) {
            if (user.getRole().getLevel() <= 3) {
                throw new CustomDeniedException("Don't have access authority");
            }
        }

        Tasks task = Tasks.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(taskStatus)
                .priority(priority)
                .taskType(taskType)
                .startDate(startDate)
                .deadline(deadline)
                .user(user)
                .department(user.getDepartment())
                .build();

        tasksRepository.save(task);
    }

    // 업무 조회 - 해당 달력의 업무
    public List<TaskResponseDto> getAllTask(TaskDateDto dto) {
        Users user = securityService.getLoginUser();
        Departments department = user.getDepartment();

        List<Tasks> tasksList = tasksRepository.findAllTask(user.getId(), dto.getStartDate(), dto.getEndDate());
        List<TaskResponseDto> tasksDtoList = tasksList.stream()
                .map(task -> new TaskResponseDto(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getPriority(),
                        task.getTaskType(),
                        task.getStartDate(),
                        task.getDeadline(),
                        user.getUsername(),
                        department.getDepartmentName()
                ))
                .collect(Collectors.toList());

        return tasksDtoList;
    }

    // 업무 수정 - 내용, 우선순위, 마감일
    @Transactional
    @TaskOwnerCheck
    public void updateTask(Long taskId, TaskUpdateRequestDto dto) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotfoundException("Invalid Task ID"));

        if (StringUtils.hasText(dto.getDescription())) {
            task.updateDescription(dto.getDescription());
        }

        if (StringUtils.hasText(dto.getPriority())) {
            task.updatePriority(TaskPriority.valueOf(dto.getPriority()));
        }

        if (dto.getDeadline() != null) {
            task.updateDeadline(dto.getDeadline());
        }

        if (StringUtils.hasText(dto.getStatus())) {
            task.updateStatus(TaskStatus.valueOf(dto.getStatus()));
        }
    }

    // 업무 수정 - 완료 상태
    @Transactional
    @TaskOwnerCheck
    public void updateTaskStatus(Long taskId, TaskStatusUpdateRequestDto dto) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotfoundException("Invalid Task ID"));

        if (StringUtils.hasText(dto.getStatus())) {
            task.updateStatus(TaskStatus.valueOf(dto.getStatus()));
        }
    }

    // 업무 삭제
    @Transactional
    @TaskOwnerCheck
    public void deleteTask(Long taskId) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotfoundException("Invalid Task ID"));

        tasksRepository.delete(task);
    }

}
