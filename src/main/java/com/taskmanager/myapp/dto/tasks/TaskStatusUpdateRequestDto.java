package com.taskmanager.myapp.dto.tasks;

import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.global.ValidEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskStatusUpdateRequestDto {

    @ValidEnum(enumClass = TaskStatus.class)
    private String status;

}
