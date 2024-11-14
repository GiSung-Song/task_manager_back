package com.taskmanager.myapp.dto.tasks;

import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import com.taskmanager.myapp.global.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRegisterRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @ValidEnum(enumClass = TaskStatus.class)
    private String taskStatus;

    @ValidEnum(enumClass = TaskPriority.class)
    private String taskPriority;

    @ValidEnum(enumClass = TaskType.class)
    private String taskType;

    private LocalDateTime deadline;
}
