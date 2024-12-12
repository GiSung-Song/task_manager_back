package com.taskmanager.myapp.repository;

import com.taskmanager.myapp.domain.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TasksRepository extends JpaRepository<Tasks, Long> {

    @Query("SELECT t FROM Tasks t JOIN t.user u " +
            "WHERE (u.id = :userId " +
            "OR (t.department.id = (SELECT u.department.id FROM Users u WHERE u.id = :userId) AND t.taskType = 'TEAM')) " +
            "AND t.deadline BETWEEN :startDate AND :endDate")
    List<Tasks> findAllTask(@Param("userId") Long userId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
}