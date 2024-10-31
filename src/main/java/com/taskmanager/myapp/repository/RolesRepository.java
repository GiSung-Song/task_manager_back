package com.taskmanager.myapp.repository;

import com.taskmanager.myapp.domain.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Long> {
}
