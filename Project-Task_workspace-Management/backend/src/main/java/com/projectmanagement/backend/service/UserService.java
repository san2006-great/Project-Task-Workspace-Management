package com.projectmanagement.backend.service;

import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.projectmanagement.backend.repository.WorkspaceRepository;
import com.projectmanagement.backend.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskRepository taskRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            TaskRepository taskRepository) {

        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.taskRepository = taskRepository;
    }

    public User saveUser(User user) {

        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null) {
            return null;
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user != null &&
                passwordEncoder.matches(
                        password,
                        user.getPassword())) {

            return user;
        }

        return null;
    }

    public User updateRole(Long id, String role) {

        User user = userRepository.findById(id)
                .orElse(null);

        if (user == null) {
            return null;
        }

        user.setRole(role);

        return userRepository.save(user);
    }
    public boolean deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElse(null);

        if (user == null) {
            return false;
        }

        if (!workspaceRepository.findByUser(user).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete user because they own workspaces"
            );
        }

        if (!taskRepository.findByAssignee(user).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete user because they are assigned to tasks"
            );
        }

        userRepository.deleteById(id);
        return true;
    }
}
