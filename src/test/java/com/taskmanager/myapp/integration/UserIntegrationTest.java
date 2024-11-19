package com.taskmanager.myapp.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.myapp.domain.Departments;
import com.taskmanager.myapp.domain.Roles;
import com.taskmanager.myapp.domain.Users;
import com.taskmanager.myapp.dto.users.UserInfoUpdateRequestDto;
import com.taskmanager.myapp.dto.users.UserPasswordRequestDto;
import com.taskmanager.myapp.dto.users.UserRegisterRequestDto;
import com.taskmanager.myapp.repository.DepartmentsRepository;
import com.taskmanager.myapp.repository.RolesRepository;
import com.taskmanager.myapp.repository.UsersRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Roles sawon;
    private Roles bujang;
    private Departments hr;
    private Departments dev;
    private Users devSawon;
    private Users hrBujang;
    private Users hrSawon;


    @BeforeEach
    void setUp() {
        sawon = Roles.createRoles("사원", 1);
        bujang = Roles.createRoles("부장", 4);

        hr = Departments.createDepartments("인사1팀");
        dev = Departments.createDepartments("개발1팀");

        rolesRepository.save(sawon);
        rolesRepository.save(bujang);

        departmentsRepository.save(hr);
        departmentsRepository.save(dev);
    }

    void setUpUsers() {
        devSawon = Users.builder()
                .username("테스터")
                .phoneNumber("01056785678")
                .password("12341234")
                .role(sawon)
                .department(dev)
                .employeeNumber("test-1234")
                .build();

        usersRepository.save(devSawon);

        hrBujang = Users.builder()
                .username("테스터2")
                .phoneNumber("01012341234")
                .password("12341234")
                .role(bujang)
                .department(hr)
                .employeeNumber("test-5678")
                .build();

        usersRepository.save(hrBujang);

        hrSawon = Users.builder()
                .username("테스터3")
                .phoneNumber("01034563456")
                .password("12341234")
                .role(sawon)
                .department(hr)
                .employeeNumber("test-1357")
                .build();

        usersRepository.save(hrSawon);
    }

    void setUpLoginUser(Users users) {
        UserDetails userDetails = User.builder()
                .username(users.getEmployeeNumber())
                .password(users.getPassword())
                .roles(users.getRole().getRoleName())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 회원가입() throws Exception {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setUsername("테스터");
        dto.setPhoneNumber("01012341234");
        dto.setPassword("12341234");
        dto.setRoleId(sawon.getId());
        dto.setDepartmentId(dev.getId());
        dto.setEmployeeNumber("test-1234");

        mockMvc.perform(post("/api/users")
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Users findUser = usersRepository.findAll().get(0);

        assertEquals(dto.getUsername(), findUser.getUsername());
        assertEquals(dto.getPhoneNumber(), findUser.getPhoneNumber());
        assertEquals(dto.getEmployeeNumber(), findUser.getEmployeeNumber());
    }

    @Test
    void 회원가입_실패_validation() throws Exception {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setUsername("테스터");
        dto.setPhoneNumber("01012341234");
        dto.setPassword("12341234");
        dto.setRoleId(sawon.getId());
        dto.setDepartmentId(dev.getId());

        mockMvc.perform(post("/api/users")
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입_실패_exist() throws Exception {
        Users users = Users.builder()
                .username("테스터")
                .phoneNumber("01012341234")
                .password("12341234")
                .role(sawon)
                .department(dev)
                .employeeNumber("test-1234")
                .build();

        usersRepository.save(users);

        UserRegisterRequestDto dto = new UserRegisterRequestDto();

        dto.setUsername("테스터");
        dto.setPhoneNumber("01012341234");
        dto.setPassword("12341234");
        dto.setRoleId(sawon.getId());
        dto.setDepartmentId(dev.getId());
        dto.setEmployeeNumber("test-1234");

        mockMvc.perform(post("/api/users")
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void 회원정보_조회_자신() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        mockMvc.perform(get("/api/users/{employeeNumber}", "test-1234"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.username").value("테스터"))
                .andExpect(jsonPath("$.data.phoneNumber").value("01056785678"));
    }

    @Test
    void 회원정보_조회_인사팀() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        mockMvc.perform(get("/api/users/{employeeNumber}", "test-1234"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.username").value("테스터"))
                .andExpect(jsonPath("$.data.phoneNumber").value("01056785678"));
    }

    @Test
    void 회원정보_조회_실패_회원없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        mockMvc.perform(get("/api/users/{employeeNumber}", "test-12341234"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원정보_조회_실패_권한없음() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        mockMvc.perform(get("/api/users/{employeeNumber}", "test-5678"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void 회원정보_수정_자신() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        dto.setPhoneNumber("01013572468");

        mockMvc.perform(patch("/api/users/{employeNumber}", "test-1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        Users findSawon = usersRepository.findByEmployeeNumber(devSawon.getEmployeeNumber());

        assertEquals("01013572468", findSawon.getPhoneNumber());
    }

    @Test
    void 회원정보_수정_인사팀() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        dto.setPhoneNumber("01013572468");

        mockMvc.perform(patch("/api/users/{employeNumber}", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        Users findSawon = usersRepository.findByEmployeeNumber(devSawon.getEmployeeNumber());

        assertEquals("01013572468", findSawon.getPhoneNumber());
    }

    @Test
    void 회원정보_수정_실패_회원없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        dto.setPhoneNumber("01013572468");

        mockMvc.perform(patch("/api/users/{employeNumber}", "test-28903293")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원정보_수정_실패_validation() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();

        mockMvc.perform(patch("/api/users/{employeNumber}", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원정보_수정_실패_권한없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrSawon);

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto();
        dto.setPhoneNumber("01013572468");

        mockMvc.perform(patch("/api/users/{employeNumber}", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_수정() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();

        dto.setPassword("737373");

        mockMvc.perform(patch("/api/users/{employeNumber}/password", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        Users findSawon = usersRepository.findByEmployeeNumber(devSawon.getEmployeeNumber());

        assertTrue(passwordEncoder.matches("737373", findSawon.getPassword()));
    }

    @Test
    void 회원_비밀번호_수정_validation() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();


        mockMvc.perform(patch("/api/users/{employeNumber}/password", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_수정_회원없음() throws Exception {
        setUpUsers();
        setUpLoginUser(devSawon);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();
        dto.setPassword("737373");

        mockMvc.perform(patch("/api/users/{employeNumber}/password", "test-1212121212")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_수정_권한없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        UserPasswordRequestDto dto = new UserPasswordRequestDto();
        dto.setPassword("737373");

        mockMvc.perform(patch("/api/users/{employeNumber}/password", devSawon.getEmployeeNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_초기화() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        mockMvc.perform(post("/api/users/{employeeNumber}/reset", devSawon.getEmployeeNumber()))
                .andExpect(status().isOk())
                .andDo(print());

        assertNotEquals("12341234", devSawon.getPassword());
    }

    @Test
    void 회원_비밀번호_초기화_회원없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrBujang);

        mockMvc.perform(post("/api/users/{employeeNumber}/reset", "1234214321"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 회원_비밀번호_초기화_권한없음() throws Exception {
        setUpUsers();
        setUpLoginUser(hrSawon);

        mockMvc.perform(post("/api/users/{employeeNumber}/reset", devSawon.getEmployeeNumber()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}
