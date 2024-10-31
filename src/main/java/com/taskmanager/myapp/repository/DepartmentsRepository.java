package com.taskmanager.myapp.repository;

import com.taskmanager.myapp.domain.Departments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentsRepository extends JpaRepository<Departments, Long> {
}
