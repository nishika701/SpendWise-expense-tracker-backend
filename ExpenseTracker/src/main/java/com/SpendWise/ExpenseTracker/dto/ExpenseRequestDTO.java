package com.SpendWise.ExpenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
