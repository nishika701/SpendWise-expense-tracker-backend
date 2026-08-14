package com.SpendWise.ExpenseTracker.service;

import com.SpendWise.ExpenseTracker.dto.AuthRequestDTO;
import com.SpendWise.ExpenseTracker.model.User;
import com.SpendWise.ExpenseTracker.repository.UserRepository;
import com.SpendWise.ExpenseTracker.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(AuthRequestDTO request){
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("User already exists");
        }

        if(request.getUsername() == null || request.getUsername().isBlank()){
            throw new RuntimeException("Username is required");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());          // add this line
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return "User registered successfully!";
    }

    public String login(AuthRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid email or password");
        }
        return jwtUtil.generateToken(user.getEmail(), user.getUsername());   // pass username too
    }
}
