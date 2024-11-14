package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.departments.DepartmentsDto;
import com.taskmanager.myapp.global.CustomResponse;
import com.taskmanager.myapp.service.DepartmentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DepartmentsController {

    private final DepartmentsService departmentsService;

    @GetMapping("/departments")
    public ResponseEntity<CustomResponse<List<DepartmentsDto>>> getAllDepartments() {
        List<DepartmentsDto> departments = departmentsService.getAllDepartments();

        return ResponseEntity.ok(CustomResponse.res(departments, "Get All Departments Successfully"));
    }

}
