package neoguide.project.controller;

import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.UserResponse;
import neoguide.project.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody(required = false) RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody(required = false) LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}