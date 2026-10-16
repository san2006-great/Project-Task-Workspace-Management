package com.projectmanagement.backend.repository;

import com.projectmanagement.backend.entity.Task;
import com.projectmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject_Workspace_User(User user);
    List<Task> findByAssignee(User user);
}