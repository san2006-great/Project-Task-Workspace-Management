package com.projectmanagement.backend.security;

import com.projectmanagement.backend.entity.User;
import com.projectmanagement.backend.repository.UserRepository;
import com.projectmanagement.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email);

            if (user != null && user.getRole() != null) {

                System.out.println(
                        "JWT USER: " + user.getEmail() +
                                " | ROLE: " + user.getRole()
                );
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "AUTHORITY: " +
                                SecurityContextHolder.getContext()
                                        .getAuthentication()
                                        .getAuthorities()
                );

                System.out.println(
                        "AUTHENTICATED: " +
                                SecurityContextHolder.getContext()
                                        .getAuthentication()
                                        .isAuthenticated()
                );
            }

        } catch (Exception e) {
            System.out.println(
                    "Invalid JWT token: " + e.getMessage()
            );
        }

        System.out.println(
                "BEFORE CHAIN: " +
                        SecurityContextHolder.getContext()
                                .getAuthentication()
        );

        filterChain.doFilter(request, response);

        System.out.println(
                "AFTER CHAIN AUTH: " +
                        SecurityContextHolder.getContext()
                                .getAuthentication()
        );
    }
}