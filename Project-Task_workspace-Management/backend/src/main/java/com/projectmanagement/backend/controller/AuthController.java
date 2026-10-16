package com.projectmanagement.backend.controller;
import com.projectmanagement.backend.dto.LoginRequest;
import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.projectmanagement.backend.service.JwtService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {

        User user = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (user == null) {
            return Map.of("message", "Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return Map.of(
                "message", "Login successful",
                "token", token,
                "user", user
        );
    }
}
