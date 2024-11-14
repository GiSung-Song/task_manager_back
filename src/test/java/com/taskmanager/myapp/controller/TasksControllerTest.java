package com.taskmanager.myapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import com.taskmanager.myapp.dto.tasks.*;
import com.taskmanager.myapp.service.TasksService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = TasksController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OncePerRequestFilter.class))
class TasksControllerTest {

    @MockBean
    private TasksService tasksService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 업무_등록_테스트() throws Exception {
        TaskRegisterRequestDto dto = new TaskRegisterRequestDto();

        dto.setTitle("테스트 제목");
        dto.setDescription("테스트 설명");
        dto.setTaskStatus("PENDING");
        dto.setTaskPriority("MEDIUM");
        dto.setTaskType("PERSONAL");
        dto.setDeadline(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/api/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        Mockito.verify(tasksService, Mockito.times(1)).addTask(any());
    }

    @Test
    void 업무_등록_실패_테스트() throws Exception {
        TaskRegisterRequestDto dto = new TaskRegisterRequestDto();

        dto.setDescription("테스트 설명");
        dto.setTaskStatus("PENDING");
        dto.setTaskPriority("MEDIUM");
        dto.setTaskType("PERSONAL");
        dto.setDeadline(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        Mockito.verify(tasksService, Mockito.times(0)).addTask(any());
    }

    @Test
    void 업무_조회_테스트() throws Exception {
        TaskResponseDto taskResponseDto1 =
                new TaskResponseDto("테스트 제목1", "테스트 설명1", TaskStatus.COMPLETED, TaskPriority.LOW, TaskType.PERSONAL,
                        LocalDateTime.now().plusDays(2), "테스터", "개발1팀");

        TaskResponseDto taskResponseDto2 =
                new TaskResponseDto("테스트 제목2", "테스트 설명2", TaskStatus.PENDING, TaskPriority.MEDIUM, TaskType.TEAM,
                        LocalDateTime.now().plusDays(9), "테스터", "개발1팀");

        List<TaskResponseDto> dto = List.of(taskResponseDto1, taskResponseDto2);

        TaskDateDto taskDateDto = new TaskDateDto();
        taskDateDto.setStartDate(LocalDateTime.now());
        taskDateDto.setEndDate(LocalDateTime.now().plusDays(10));

        when(tasksService.getAllTask(any())).thenReturn(dto);

        mockMvc.perform(get("/api/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("테스트 제목1"))
                .andExpect(jsonPath("$.data[1].title").value("테스트 제목2"))
                .andDo(print());
    }

    @Test
    void 업무_수정_테스트() throws Exception {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setStatus("PENDING");

        mockMvc.perform(patch("/api/task/{id}", 0L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(tasksService, times(1)).updateTask(any(), any());
    }

    @Test
    void 업무_상태_수정_테스트() throws Exception {
        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();

        dto.setStatus("PENDING");

        mockMvc.perform(patch("/api/task/{id}/status", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(tasksService, times(1)).updateTaskStatus(any(), any());
    }

    @Test
    void 업무_삭제_테스트() throws Exception {
        mockMvc.perform(delete("/api/task/{id}", 0L))
                .andExpect(status().isOk())
                .andDo(print());

        verify(tasksService, times(1)).deleteTask(any());
    }

}