package com.taskmanager.myapp.integration;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
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

    private Users user;
    private Roles roles;
    private Departments departments;
    private Tasks task1;
    private Tasks task2;

    @BeforeEach
    void setUp() {
        if (roles != null && rolesRepository.existsById(roles.getId())) {
            roles = rolesRepository.findById(roles.getId()).get();

            System.out.println("roleName : " + roles.getRoleName());
        } else {
            roles = Roles.createRoles("부장", 4);
            rolesRepository.save(roles);
        }

        if (departments != null && departmentsRepository.existsById(departments.getId())) {
            departments = departmentsRepository.findById(departments.getId()).get();
        } else {
            departments = Departments.createDepartments("개발1팀");
            departmentsRepository.save(departments);
        }

        user = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터")
                .phoneNumber("01012345678")
                .employeeNumber("12345")
                .password("encodedPassword")
                .build();

        usersRepository.save(user);

        // Authentication 설정
        UserDetails userDetails = User.builder()
                .username(user.getEmployeeNumber())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 기본적인 업무 데이터 설정
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

        tasksRepository.save(task1);
        tasksRepository.save(task2);
    }

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

        Tasks task = tasksRepository.findAll().get(0);

        Assertions.assertEquals("테스트 제목", task.getTitle());
        Assertions.assertEquals(TaskType.PERSONAL, task.getTaskType());
    }

    @Test
    void 업무_등록_실패_테스트() throws Exception {
        Roles roles = Roles.createRoles("사원", 1);
        rolesRepository.save(roles);

        Users user = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터2")
                .phoneNumber("01012341234")
                .employeeNumber("131313")
                .password("encodedPassword")
                .build();

        usersRepository.save(user);

        UserDetails userDetails = User.builder()
                .username(user.getEmployeeNumber())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

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
        mockMvc.perform(get("/api/task"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("테스트 제목"))
                .andExpect(jsonPath("$.data[1].title").value("테스트 제목2"));
    }

    @Test
    void 업무_수정_테스트() throws Exception {
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
        Assertions.assertEquals("테스트 제목", tasks.getTitle());
    }

    @Test
    void 업무_수정_실패_테스트() throws Exception {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto();

        dto.setPriority("HIGH");
        dto.setDeadline(LocalDateTime.now().plusDays(15));

        mockMvc.perform(patch("/api/task/{id}", 1324321L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 업무_상태_수정_테스트() throws Exception {
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
        TaskStatusUpdateRequestDto dto = new TaskStatusUpdateRequestDto();

        dto.setStatus("COMPLETED");

        mockMvc.perform(patch("/api/task/{id}", 1324321L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 업무_주인_체크_테스트() throws Exception {
        Users user2 = Users.builder()
                .role(roles)
                .department(departments)
                .username("테스터222")
                .phoneNumber("1234123412")
                .employeeNumber("1234512345")
                .password("encodedPassword")
                .build();

        usersRepository.save(user2);

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
        mockMvc.perform(delete("/api/task/{id}", task1.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        Optional<Tasks> deletedTask = tasksRepository.findById(task1.getId());

        Assertions.assertTrue(deletedTask.isEmpty());
    }
}
