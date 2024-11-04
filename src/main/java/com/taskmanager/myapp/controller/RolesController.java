package com.taskmanager.myapp.controller;

import com.taskmanager.myapp.dto.RolesDto;
import com.taskmanager.myapp.service.RolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RolesController {

    private final RolesService rolesService;

    @GetMapping("/roles")
    public ResponseEntity<List<RolesDto>> getAllRoles() {
        List<RolesDto> rolesDtoList = rolesService.getAllRoles();

        return ResponseEntity.ok(rolesDtoList);
    }

}
