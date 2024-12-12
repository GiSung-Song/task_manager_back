package com.taskmanager.myapp.dto.tasks;

import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.global.ValidEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TaskUpdateRequestDto {

    private String description;

    @ValidEnum(enumClass = TaskPriority.class)
    private String priority;

    private LocalDateTime deadline;

    @ValidEnum(enumClass = TaskStatus.class)
    private String taskStatus;
}
