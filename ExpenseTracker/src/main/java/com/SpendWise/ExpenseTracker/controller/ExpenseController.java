package com.SpendWise.ExpenseTracker.controller;

import com.SpendWise.ExpenseTracker.dto.ExpenseRequestDTO;
import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody ExpenseRequestDTO expenseRequestDTO){
        Expense savedExpense = expenseService.createExpense(expenseRequestDTO);
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Expense> getAllExpenses(){
        return expenseService.getAllExpenses();
    }
}
