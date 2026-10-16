package com.projectmanagement.backend.repository;

import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByUser(User user);
}