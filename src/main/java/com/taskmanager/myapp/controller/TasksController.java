package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.tasks.*;
import com.taskmanager.myapp.global.CustomResponse;
import com.taskmanager.myapp.service.TasksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TasksController {

    private final TasksService tasksService;

    // 업무 등록
    @PostMapping("/task")
    public ResponseEntity<CustomResponse<String>> addTask(@RequestBody @Valid TaskRegisterRequestDto dto) {
        tasksService.addTask(dto);

        log.info("start date : {}", dto.getStartDate());
        log.info("end date : {}", dto.getDeadline());

        return ResponseEntity.ok(CustomResponse.res(null, "업무를 등록했습니다."));
    }

    // 업무 조회 - 해당 월 별
    @GetMapping("/task")
    public ResponseEntity<CustomResponse<List<TaskResponseDto>>> getTaskList(
            @RequestParam String startDate, @RequestParam String endDate) {

        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);

        TaskDateDto dto = new TaskDateDto();

        dto.setStartDate(start);
        dto.setEndDate(end);

        List<TaskResponseDto> allTask = tasksService.getAllTask(dto);

        return ResponseEntity.ok(CustomResponse.res(allTask, "업무를 조회했습니다."));
    }

    // 업무 수정 - 내용, 우선순위, 마감일, 업무 상태
    @PatchMapping("/task/{taskId}")
    public ResponseEntity<CustomResponse<String>> editTask(@PathVariable("taskId") Long taskId, @RequestBody @Valid TaskUpdateRequestDto dto) {
        tasksService.updateTask(taskId, dto);

        return ResponseEntity.ok(CustomResponse.res(null, "업무를 수정했습니다."));
    }

    // 업무 수정 - 업무 상태
    @PatchMapping("/task/{taskId}/status")
    public ResponseEntity<CustomResponse<String>> editTaskStatus(@PathVariable("taskId") Long taskId, @RequestBody @Valid TaskStatusUpdateRequestDto dto) {
        tasksService.updateTaskStatus(taskId, dto);

        return ResponseEntity.ok(CustomResponse.res(null, "업무 상태를 변경했습니다."));
    }

    // 업무 삭제
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<CustomResponse<String>> deleteTask(@PathVariable("taskId") Long taskId) {
        tasksService.deleteTask(taskId);

        return ResponseEntity.ok(CustomResponse.res(null, "업무를 삭제했습니다."));
    }
}
