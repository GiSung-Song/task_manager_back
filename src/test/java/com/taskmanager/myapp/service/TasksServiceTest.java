package com.taskmanager.myapp.service;

import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Tasks;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import com.taskmanager.myapp.dto.tasks.*;
import com.taskmanager.myapp.exception.CustomDeniedException;
import com.taskmanager.myapp.exception.ResourceNotfoundException;
import com.taskmanager.myapp.repository.TasksRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TasksServiceTest {

    @InjectMocks
    private TasksService tasksService;

    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private SecurityService securityService;

    private Users getUser() {
        return Users.builder()
                .id(0L)
                .role(Roles.createRoles("차장", 1))
                .department(Departments.createDepartments("개발1팀"))
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("12345")
                .password("encodedPassword")
                .build();
    }

    @Test
    void 업무_등록_테스트() {
        Users user = getUser();
        when(securityService.getLoginUser()).thenReturn(user);

        TaskRegisterRequestDto dto = new TaskRegisterRequestDto();

        dto.setTitle("테스트 제목");
        dto.setDescription("테스트 설명");

        tasksService.addTask(dto);

        verify(tasksRepository, times(1)).save(any(Tasks.class));
    }

    @Test
    void 업무_등록_실패_테스트() {
        Users user = getUser();
        when(securityService.getLoginUser()).thenReturn(user);

        TaskRegisterRequestDto dto = new TaskRegisterRequestDto();

        dto.setTitle("테스트 제목");
        dto.setDescription("테스트 설명");
        dto.setTaskType("TEAM");

        assertThrows(CustomDeniedException.class, () -> tasksService.addTask(dto));
    }

    @Test
    void 업무_조회_테스트() {
        Users user = getUser();
        when(securityService.getLoginUser()).thenReturn(user);
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task1 = Tasks.builder()
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        Tasks task2 = Tasks.builder()
                .title("테스트 제목2")
                .description("테스트 설명2")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.LOW)
                .taskType(TaskType.TEAM)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        TaskDateDto dto = new TaskDateDto();
        LocalDateTime now = LocalDateTime.now();
        dto.setStartDate(now);
        dto.setEndDate(now.plusDays(2));

        when(tasksRepository.findAllTask(user.getId(), now, now.plusDays(2))).thenReturn(List.of(task1, task2));

        List<TaskResponseDto> allTask = tasksService.getAllTask(dto);

        assertEquals(2, allTask.size());
        assertEquals("테스트 제목", allTask.get(0).getTitle());
        assertEquals("테스트 제목2", allTask.get(1).getTitle());
    }

    @Test
    void 업무_수정_테스트() {
        Users user = getUser();
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task = Tasks.builder()
                .id(0L)
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        when(tasksRepository.findById(any())).thenReturn(Optional.of(task));

        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setDescription("수정된 설명");
        dto.setPriority("LOW");

        tasksService.updateTask(0L, dto);

        assertEquals("수정된 설명", task.getDescription());
        assertEquals(TaskPriority.LOW, task.getPriority());
    }

    @Test
    void 업무_수정_실패_테스트() {
        Users user = getUser();
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task = Tasks.builder()
                .id(0L)
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setDescription("수정된 설명");
        dto.setPriority("LOW");

        when(tasksRepository.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotfoundException.class, () -> tasksService.updateTask(0L, dto));
    }

    @Test
    void 업무_상태_수정_테스트() {
        Users user = getUser();
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task = Tasks.builder()
                .id(0L)
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        when(tasksRepository.findById(any())).thenReturn(Optional.of(task));

        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();
        dto.setStatus("COMPLETED");

        tasksService.updateTaskStatus(0L, dto);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void 업무_삭제_테스트() {
        Users user = getUser();
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task = Tasks.builder()
                .id(0L)
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        when(tasksRepository.findById(any())).thenReturn(Optional.of(task));

        tasksService.deleteTask(0L);

        verify(tasksRepository, times(1)).delete(task);
    }

    @Test
    void 업무_삭제_실패_테스트() {
        Users user = getUser();
        Departments departments = Departments.createDepartments("개발 1팀");

        Tasks task = Tasks.builder()
                .id(0L)
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        when(tasksRepository.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotfoundException.class, () -> tasksService.deleteTask(0L));
    }
}