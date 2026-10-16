package com.projectmanagement.backend.service;

import com.projectmanagement.backend.entity.Task;
import com.projectmanagement.backend.entity.TaskComment;
import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.repository.TaskCommentRepository;
import com.projectmanagement.backend.repository.TaskRepository;
import com.projectmanagement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskCommentService(
            TaskCommentRepository taskCommentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Get current logged-in user
    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found"
            );
        }

        return user;
    }

    // Check whether the current user can access the task
    private Task getAccessibleTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElse(null);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }

        User currentUser = getCurrentUser();

        // ADMIN can access any task
        if ("ADMIN".equals(currentUser.getRole())) {
            return task;
        }

        // Task owner can access the task
        if (task.getProject() != null &&
                task.getProject().getWorkspace() != null &&
                task.getProject().getWorkspace().getUser() != null &&
                task.getProject().getWorkspace().getUser().getId()
                        .equals(currentUser.getId())) {

            return task;
        }

        // Assigned user can access the task
        if (task.getAssignee() != null &&
                task.getAssignee().getId()
                        .equals(currentUser.getId())) {

            return task;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to access comments for this task"
        );
    }

    // Get all comments for a task
    public List<TaskComment> getComments(Long taskId) {

        Task task = getAccessibleTask(taskId);

        return taskCommentRepository.findByTask(task);
    }

    // Add comment
    public TaskComment addComment(
            Long taskId,
            String commentText) {

        Task task = getAccessibleTask(taskId);

        if (commentText == null ||
                commentText.trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment cannot be empty"
            );
        }

        User currentUser = getCurrentUser();

        TaskComment taskComment = new TaskComment();

        taskComment.setComment(commentText.trim());
        taskComment.setTask(task);
        taskComment.setUser(currentUser);

        return taskCommentRepository.save(taskComment);
    }

    // Update own comment
    public TaskComment updateComment(
            Long commentId,
            String commentText) {

        TaskComment taskComment =
                taskCommentRepository.findById(commentId)
                        .orElse(null);

        if (taskComment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        User currentUser = getCurrentUser();

        if (taskComment.getUser() == null ||
                !taskComment.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only edit your own comment"
            );
        }

        if (commentText == null ||
                commentText.trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment cannot be empty"
            );
        }

        taskComment.setComment(commentText.trim());

        return taskCommentRepository.save(taskComment);
    }

    // Delete comment
    public boolean deleteComment(Long commentId) {

        TaskComment taskComment =
                taskCommentRepository.findById(commentId)
                        .orElse(null);

        if (taskComment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        User currentUser = getCurrentUser();

        // ADMIN can delete any comment
        if ("ADMIN".equals(currentUser.getRole())) {

            taskCommentRepository.delete(taskComment);

            return true;
        }

        // USER can delete only their own comment
        if (taskComment.getUser() == null ||
                !taskComment.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only delete your own comment"
            );
        }

        taskCommentRepository.delete(taskComment);

        return true;
    }
}