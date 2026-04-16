package com.SpendWise.ExpenseTracker.controller;


import com.SpendWise.ExpenseTracker.dto.AuthRequestDTO;
import com.SpendWise.ExpenseTracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequestDTO request){
        String response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequestDTO request){
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }

}

/*

ResponseEntity<> is used when you want full control over the HTTP response.

*/