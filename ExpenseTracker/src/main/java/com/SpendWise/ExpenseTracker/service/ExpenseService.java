package com.SpendWise.ExpenseTracker.service;

import com.SpendWise.ExpenseTracker.dto.ExpenseRequestDTO;
import com.SpendWise.ExpenseTracker.exception.CustomExceptions;
import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.model.User;
import com.SpendWise.ExpenseTracker.repository.ExpenseRepository;
import com.SpendWise.ExpenseTracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,UserRepository userRepository){
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public Expense createExpense(ExpenseRequestDTO expenseRequestDTO){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        //getContext() - used for getting current security context like find out who is making this request
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(expenseRequestDTO.getTitle());
        expense.setAmount(expenseRequestDTO.getAmount());
        expense.setCategory(expenseRequestDTO.getCategory());
        expense.setDate(expenseRequestDTO.getDate());
        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
        return expenseRepository.findByUser(user);
    }

}

/*
• DTO = what user typed
• entity = what database stores
• service = combines user input + logged-in user + business rules
*/