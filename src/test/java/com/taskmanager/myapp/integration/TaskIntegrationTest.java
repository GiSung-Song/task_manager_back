package com.taskmanager.myapp.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Tasks;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.domain.enums.TaskPriority;
import com.taskmanager.myapp.domain.enums.TaskStatus;
import com.taskmanager.myapp.domain.enums.TaskType;
import com.taskmanager.myapp.dto.tasks.TaskRegisterRequestDto;
import com.taskmanager.myapp.dto.tasks.TaskStatusUpdateRequestDto;
import com.taskmanager.myapp.dto.tasks.TaskUpdateRequestDto;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.TasksRepository;
import com.taskmanager.myapp.repository.UsersRepository;
import com.taskmanager.myapp.service.TasksService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TasksService tasksService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksRepository tasksRepository;

    Users user;
    Roles roles;
    Departments departments;
    Tasks task1;
    Tasks task2;

    void setUpTasks() {
        task1 = Tasks.builder()
                .title("테스트 제목")
                .description("테스트 설명")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .taskType(TaskType.PERSONAL)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        task2 = Tasks.builder()
                .title("테스트 제목2")
                .description("테스트 설명2")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.LOW)
                .taskType(TaskType.TEAM)
                .deadline(LocalDateTime.now().plusDays(2))
                .user(user)
                .department(departments)
                .build();

        tasksRepository.saveAndFlush(task1);
        tasksRepository.saveAndFlush(task2);
    }

    void setUpBuJang() {
        roles = Roles.createRoles("부장");
        departments = Departments.createDepartments("개발1팀");

        rolesRepository.saveAndFlush(roles);
        departmentsRepository.saveAndFlush(departments);

        user = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("12345")
                .password("encodedPassword")
                .build();

        usersRepository.saveAndFlush(user);

        UserDetails userDetails = User.builder()
                .username(user.getEmployeeNumber())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    void setUpSawon() {
        Roles roles = Roles.createRoles("사원");
        Departments department = Departments.createDepartments("개발1팀");

        rolesRepository.saveAndFlush(roles);
        departmentsRepository.saveAndFlush(department);

        Users user = Users.builder()
                .role(roles)
                .department(department)
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("12345")
                .password("encodedPassword")
                .build();

        usersRepository.saveAndFlush(user);

        UserDetails userDetails = User.builder()
                .username(user.getEmployeeNumber())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 업무_등록_테스트() throws Exception {
        setUpBuJang();
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

        Tasks task = tasksRepository.findAll().get(0);

        Assertions.assertEquals("테스트 제목", task.getTitle());
        Assertions.assertEquals(TaskType.PERSONAL, task.getTaskType());
    }

    @Test
    void 업무_등록_실패_테스트() throws Exception {
        setUpSawon();
        TaskRegisterRequestDto dto = new TaskRegisterRequestDto();

        dto.setTitle("테스트 제목");
        dto.setDescription("테스트 설명");
        dto.setTaskStatus("PENDING");
        dto.setTaskPriority("MEDIUM");
        dto.setTaskType("TEAM");
        dto.setDeadline(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void 업무_조회_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        mockMvc.perform(get("/api/task"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("테스트 제목"))
                .andExpect(jsonPath("$.data[1].title").value("테스트 제목2"));
    }

    @Test
    void 업무_수정_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setPriority("HIGH");
        dto.setDeadline(LocalDateTime.now().plusDays(15));

        mockMvc.perform(patch("/api/task/{id}", task1.getId())
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Tasks tasks = tasksRepository.findById(task1.getId()).get();

        Assertions.assertEquals(TaskPriority.HIGH, tasks.getPriority());
        Assertions.assertEquals(
                LocalDateTime.now().plusDays(15).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                tasks.getDeadline().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        Assertions.assertEquals("테스트 제목", tasks.getTitle());
    }

    @Test
    void 업무_수정_실패_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setPriority("HIGH");
        dto.setDeadline(LocalDateTime.now().plusDays(15));

        mockMvc.perform(patch("/api/task/{id}", 1324321L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void 업무_상태_수정_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();

        dto.setStatus("COMPLETED");

        mockMvc.perform(patch("/api/task/{id}/status", task1.getId())
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Tasks tasks = tasksRepository.findById(task1.getId()).get();

        Assertions.assertEquals(TaskStatus.COMPLETED, tasks.getStatus());
        Assertions.assertEquals("테스트 제목", tasks.getTitle());
    }

    @Test
    void 업무_수정_상태_실패_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();

        dto.setStatus("COMPLETED");

        mockMvc.perform(patch("/api/task/{id}", 1324321L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void 업무_주인_체크_테스트() throws Exception {
        roles = Roles.createRoles("부장");
        departments = Departments.createDepartments("개발1팀");

        rolesRepository.saveAndFlush(roles);
        departmentsRepository.saveAndFlush(departments);

        user = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("12345")
                .password("encodedPassword")
                .build();

        usersRepository.saveAndFlush(user);

        setUpTasks();

        Users user2 = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터222")
                .phoneNumber("1234123412")
                .employeeNumber("1234512345")
                .password("encodedPassword")
                .build();

        usersRepository.saveAndFlush(user2);

        UserDetails userDetails = User.builder()
                .username(user2.getEmployeeNumber())
                .password(user2.getPassword())
                .roles(user2.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("123412341234" + SecurityContextHolder.getContext().getAuthentication().getName());
        
        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();

        dto.setStatus("COMPLETED");

        mockMvc.perform(patch("/api/task/{id}", task1.getId())
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void 업무_삭제_테스트() throws Exception {
        setUpBuJang();
        setUpTasks();

        mockMvc.perform(delete("/api/task/{id}", task1.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Tasks> deletedTask = tasksRepository.findById(task1.getId());

        Assertions.assertTrue(deletedTask.isEmpty());
    }
}
