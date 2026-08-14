package com.SpendWise.ExpenseTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {

    @Email(message = "Enter a valid email")//checks valid email format
    @NotBlank(message = "Email is required")// prevents empty input
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String username;
}
