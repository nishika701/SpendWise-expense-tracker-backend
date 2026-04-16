package com.SpendWise.ExpenseTracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @ManyToOne //many expense records may belong to one user
    @JoinColumn(name = "user_id") //tells JPA which database column stores that link. In the expense table, there will be a user_id column. That column points to the id of a row in the users table.
    private User user;
}
