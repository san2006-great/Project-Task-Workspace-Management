package com.projectmanagement.backend.repository;

import com.projectmanagement.backend.entity.TaskComment;
import com.projectmanagement.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository
        extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTask(Task task);
}