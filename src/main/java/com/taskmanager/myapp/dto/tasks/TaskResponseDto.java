package com.taskmanager.myapp.dto.tasks;

import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {

    private Long taskId;
    private String title;
    private String description;
    private TaskStatus taskStatus;
    private TaskPriority priority;
    private TaskType taskType;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private String owner;
    private String ownerEmployeeNumber;
    private String department;
}
