package com.projectmanagement.backend.service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.projectmanagement.backend.entity.Task;
import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.repository.TaskRepository;
import com.projectmanagement.backend.repository.UserRepository;
import com.projectmanagement.backend.repository.ProjectRepository;
import com.projectmanagement.backend.entity.Project;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.projectmanagement.backend.entity.Notification;
import com.projectmanagement.backend.service.NotificationService;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            NotificationService notificationService) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
    }

    // Create task
    public Task saveTask(Task task) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email);

        if (task.getProject() == null ||
                task.getProject().getId() == null) {
            return null;
        }

        Project project = projectRepository
                .findById(task.getProject().getId())
                .orElse(null);

        if (project == null ||
                project.getWorkspace() == null ||
                project.getWorkspace().getUser() == null) {
            return null;
        }

        if (!project.getWorkspace()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {
            return null;
        }

        task.setProject(project);

        // Set the logged-in user as the task creator
        task.setCreatedBy(currentUser);

        if (task.getAssignee() != null &&
                task.getAssignee().getId() != null) {

            User assignee = userRepository
                    .findById(task.getAssignee().getId())
                    .orElse(null);

            if (assignee != null) {
                task.setAssignee(assignee);
            }
        }

        Task savedTask = taskRepository.save(task);

        if (savedTask.getAssignee() != null) {
            notifyAssignee(savedTask);
        }

        return savedTask;
    }

    // Get all tasks
    public List<Task> getAllTasks() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        // ADMIN can see all tasks
        if ("ADMIN".equals(user.getRole())) {
            return taskRepository.findAll();
        }

        // USER can see:
        // 1. Tasks from their own workspace
        // 2. Tasks assigned to them

        List<Task> workspaceTasks =
                taskRepository.findByProject_Workspace_User(user);

        List<Task> assignedTasks =
                taskRepository.findByAssignee(user);

        // Add assigned tasks that are not already present
        for (Task task : assignedTasks) {

            if (!workspaceTasks.contains(task)) {
                workspaceTasks.add(task);
            }
        }

        return workspaceTasks;
    }

    // Get task by ID
    public Task getTaskById(Long id) {

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return null;
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (task.getProject() == null ||
                task.getProject().getWorkspace() == null ||
                task.getProject().getWorkspace().getUser() == null) {
            return null;
        }

        if (!task.getProject()
                .getWorkspace()
                .getUser()
                .getId()
                .equals(user.getId())) {
            return null;
        }

        return task;
    }

    // Update task status
    public Task updateStatus(Long id, String status) {

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email);

        if (task.getProject() == null ||
                task.getProject().getWorkspace() == null ||
                task.getProject().getWorkspace().getUser() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this task"
            );
        }

        if (!task.getProject()
                .getWorkspace()
                .getUser()
                .getId()
                .equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this task"
            );
        }

        task.setStatus(status);

        return taskRepository.save(task);
    }

    // Update complete task
    public Task updateTask(Long id, Task updatedTask) {

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email);

        if (task.getProject() == null ||
                task.getProject().getWorkspace() == null ||
                task.getProject().getWorkspace().getUser() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this task"
            );
        }

        if (!task.getProject()
                .getWorkspace()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this task"
            );
        }

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setPriority(updatedTask.getPriority());
        task.setDueDate(updatedTask.getDueDate());

        if (updatedTask.getAssignee() != null &&
                updatedTask.getAssignee().getId() != null) {

            Long userId = updatedTask.getAssignee().getId();

            User newAssignee = userRepository
                    .findById(userId)
                    .orElse(null);

            if (newAssignee != null) {

                Long previousAssigneeId = task.getAssignee() != null
                        ? task.getAssignee().getId()
                        : null;

                task.setAssignee(newAssignee);

                if (!newAssignee.getId().equals(previousAssigneeId)) {
                    // Save before creating the notification
                    taskRepository.save(task);
                    notifyAssignee(task);
                }
            }
        }

        return taskRepository.save(task);
    }

    // Assign task to user
    public Task assignTask(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email);

        if (task.getProject() == null ||
                task.getProject().getWorkspace() == null ||
                task.getProject().getWorkspace().getUser() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to assign this task"
            );
        }

        if (!task.getProject()
                .getWorkspace()
                .getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to assign this task"
            );
        }

        User assignee = userRepository
                .findById(userId)
                .orElse(null);

        if (assignee == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Assignee not found"
            );
        }

        Long previousAssigneeId = task.getAssignee() != null
                ? task.getAssignee().getId()
                : null;

        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);

        if (!assignee.getId().equals(previousAssigneeId)) {
            notifyAssignee(savedTask);
        }

        return savedTask;

    }

    // Delete task
    public boolean deleteTask(Long id) {

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return false;
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email);

        // ADMIN can delete any task
        if ("ADMIN".equals(currentUser.getRole())) {
            taskRepository.deleteById(id);
            return true;
        }

        // USER can delete only their own created task
        if (task.getCreatedBy() != null &&
                task.getCreatedBy().getId()
                        .equals(currentUser.getId())) {

            taskRepository.deleteById(id);
            return true;
        }

        // USER cannot delete another user's task
        return false;
    }

    private void notifyAssignee(Task task) {
        if (task.getAssignee() != null
                && task.getAssignee().getId() != null) {

            String message = "You have been assigned a new task: "
                    + task.getTitle();

            notificationService.createNotification(
                    task.getAssignee().getId(),
                    message
            );
        }
    }
}