package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * User Registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO userDTO = userService.registerUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getPhone()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please verify your email.");
            response.put("user", userDTO);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details
            User user = userService.getUserByEmail(loginRequest.getEmail());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
            String refreshToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            // Build response
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setUserId(user.getId());
            loginResponse.setEmail(user.getEmail());
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setRole(user.getRole().toString());

            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Invalid email or password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Refresh Token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix

            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                User user = userService.getUserByEmail(email);

                String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

                Map<String, Object> response = new HashMap<>();
                response.put("token", newToken);
                response.put("message", "Token refreshed successfully");

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid or expired token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Get Current User Profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                UserDTO userDTO = userService.getUserById(userService.getUserByEmail(email).getId());
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid or expired token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Change Password
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            String jwt = token.substring(7);
            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                User user = userService.getUserByEmail(email);
                userService.changePassword(user.getId(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Password changed successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid or expired token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Logout
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
