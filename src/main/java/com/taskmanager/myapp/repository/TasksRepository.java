package com.taskmanager.myapp.repository;

import com.taskmanager.myapp.domain.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
}
