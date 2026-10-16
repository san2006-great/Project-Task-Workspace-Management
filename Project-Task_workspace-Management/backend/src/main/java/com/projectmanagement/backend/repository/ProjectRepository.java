package com.projectmanagement.backend.repository;

import com.projectmanagement.backend.entity.Project;
import com.projectmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspace_User(User user);
}