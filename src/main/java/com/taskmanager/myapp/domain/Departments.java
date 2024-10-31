package com.taskmanager.myapp.domain;

import com.taskmanager.myapp.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Departments extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String departmentName;

    public static Departments of(String departmentName) {
        Departments departments = new Departments();

        departments.setDepartmentName(departmentName);

        return departments;
    }

    private void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
