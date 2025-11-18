package org.ashahar.authservice.service;

import io.jsonwebtoken.JwtException;
import org.ashahar.authservice.dto.LoginRequestDto;

import org.ashahar.authservice.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }


    public Optional<String> authenticate(LoginRequestDto loginRequestDTO) {

        return userService
                .findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));
    }

    public boolean validateToken(String substring) {
        try {
            jwtUtil.validateToken(substring);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
