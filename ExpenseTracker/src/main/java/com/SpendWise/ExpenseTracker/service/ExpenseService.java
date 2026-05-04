package com.SpendWise.ExpenseTracker.service;

import com.SpendWise.ExpenseTracker.dto.ExpenseRequestDTO;
import com.SpendWise.ExpenseTracker.exception.CustomExceptions;
import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.model.User;
import com.SpendWise.ExpenseTracker.repository.ExpenseRepository;
import com.SpendWise.ExpenseTracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Expense getExpense(Long id){
        return expenseRepository.findById(id).orElseThrow(()-> new RuntimeException("Expense not found!"));
    }

    public Expense updateOneExpense(Long id,String fieldName,String newValue){
        Expense expense = expenseRepository.findById(id).orElseThrow(()-> new RuntimeException("Expense not found"));
        switch(fieldName){
            case "title":
                expense.setTitle(newValue);
                break;
            case "amount":
                expense.setAmount(Double.parseDouble(newValue));
                break;
            case "category":
                expense.setCategory(newValue);
                break;
            case "date":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                expense.setDate(LocalDate.parse(newValue, formatter));
                break;
            default:
                throw new RuntimeException("Invalid field name");
        }
        return expenseRepository.save(expense);
    }

    public Expense updateFields(Long id,ExpenseRequestDTO expenseRequestDTO){
        Expense expense = expenseRepository.findById(id).orElseThrow(()-> new RuntimeException("Expense not found"));
        expense.setTitle(expenseRequestDTO.getTitle());
        expense.setAmount(expenseRequestDTO.getAmount());
        expense.setDate(expenseRequestDTO.getDate());
        expense.setCategory(expenseRequestDTO.getCategory());
        return expenseRepository.save(expense);
    }

    public String deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Invalid ID!"));
        expenseRepository.delete(expense);
        return "Expense deleted successfully";
    }

    public List<Expense> filterByCategory(String categoryName){
        List<Expense> exp = new ArrayList<>();
        List<Expense> expenses = getAllExpenses();
        for(Expense e : expenses){
            if(e.getCategory().equalsIgnoreCase(categoryName)){
                exp.add(e);
            }
        }
        return exp;
    }

    public List<Expense> filterByDateRange(LocalDate startDate,LocalDate endDate){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new CustomExceptions.UserNotFoundException("User not found"));
        return expenseRepository.findByUserAndDateBetween(user,startDate,endDate);
    }

    public Double getMonthlyTotal(int month,int year){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new CustomExceptions.UserNotFoundException("User not found"));

        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user,startDate,endDate);
        return expenses.stream().mapToDouble(Expense::getAmount).sum();

    }

    public Map<String,Double> getCategoryWiseTotal(){
        List<Expense> expenses = getAllExpenses();

        return expenses.stream().collect(Collectors.groupingBy(Expense::getCategory,Collectors.summingDouble(Expense::getAmount)));
    }

    public List<Expense> getAllExpensesSorted(String sortBy){
        List<Expense> expenses = getAllExpenses();
        switch (sortBy){
            case "amount":
                expenses.sort(Comparator.comparing(Expense::getAmount));
                break;
            case "date":
                expenses.sort(Comparator.comparing(Expense::getDate));
                break;
            case "id":
                expenses.sort(Comparator.comparing(Expense::getId));
                break;
            case "title":
                expenses.sort(Comparator.comparing(Expense::getTitle));
                break;
            case "category":
                expenses.sort(Comparator.comparing(Expense::getCategory));
                break;
            default:
                throw new RuntimeException("Invalid sort field:" + sortBy);
        }
        return expenses;
    }

    public List<Expense> searchByTitle(String title){
        List<Expense> expenses = getAllExpenses();
        List<Expense> result = new ArrayList<>();

        for(Expense exp : expenses){
            if(exp.getTitle().toLowerCase().contains(title.toLowerCase())){
                result.add(exp);
            }
        }
        return expenses;
    }
}

/*
• DTO = what user typed
• entity = what database stores
• service = combines user input + logged-in user + business rules
*/