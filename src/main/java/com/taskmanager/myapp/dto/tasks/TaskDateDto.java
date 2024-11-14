package com.taskmanager.myapp.dto.tasks;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TaskDateDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
