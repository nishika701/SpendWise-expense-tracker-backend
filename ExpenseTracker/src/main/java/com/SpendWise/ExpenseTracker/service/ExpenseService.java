package com.SpendWise.ExpenseTracker.service;

import com.SpendWise.ExpenseTracker.dto.ExpenseRequestDTO;
import com.SpendWise.ExpenseTracker.dto.ExpenseResponseDTO;
import com.SpendWise.ExpenseTracker.exception.CustomExceptions;
import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.model.User;
import com.SpendWise.ExpenseTracker.repository.ExpenseRepository;
import com.SpendWise.ExpenseTracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private ExpenseResponseDTO mapToResponseDTO(Expense expense){
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setDate(expense.getDate());
        return dto;
    }

    public ExpenseService(ExpenseRepository expenseRepository,UserRepository userRepository){
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(()->new CustomExceptions.UserNotFoundException("User not found"));
    }

    private Expense getExpenseForCurrentUser(Long id){
        User user = getCurrentUser();
        return  expenseRepository.findByIdAndUser(id,user).orElseThrow(()-> new RuntimeException("Expense not found"));
    }

    public ExpenseResponseDTO createExpense(ExpenseRequestDTO expenseRequestDTO){
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        //getContext() - used for getting current security context like find out who is making this request
        User user = getCurrentUser();
        Expense expense = new Expense();
        expense.setTitle(expenseRequestDTO.getTitle());
        expense.setAmount(expenseRequestDTO.getAmount());
        expense.setCategory(expenseRequestDTO.getCategory());
        expense.setDate(expenseRequestDTO.getDate());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponseDTO(savedExpense);
    }

    public List<ExpenseResponseDTO> getAllExpenses(){
        User user = getCurrentUser();
        return  expenseRepository.findByUser(user).stream().map(this::mapToResponseDTO).toList();
    }

    public ExpenseResponseDTO getExpense(Long id){
        Expense expense = getExpenseForCurrentUser(id);
        return mapToResponseDTO(expense);
    }

    public ExpenseResponseDTO updateOneExpense(Long id,String fieldName,String newValue){
        Expense expense = getExpenseForCurrentUser(id);
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
        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponseDTO(savedExpense);
    }

    public ExpenseResponseDTO updateFields(Long id,ExpenseRequestDTO expenseRequestDTO){
        Expense expense = getExpenseForCurrentUser(id);
        expense.setTitle(expenseRequestDTO.getTitle());
        expense.setAmount(expenseRequestDTO.getAmount());
        expense.setDate(expenseRequestDTO.getDate());
        expense.setCategory(expenseRequestDTO.getCategory());

        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponseDTO(savedExpense);
    }

    public String deleteExpense(Long id) {
        Expense expense = getExpenseForCurrentUser(id);
        expenseRepository.delete(expense);
        return "Expense deleted successfully";
    }

    public List<ExpenseResponseDTO> filterByCategory(String categoryName){
        User user = getCurrentUser();

        return expenseRepository.findByUser(user).stream().filter(e -> e.getCategory().equalsIgnoreCase(categoryName))
                .map(this::mapToResponseDTO).toList();
    }

    public List<ExpenseResponseDTO> filterByDateRange(LocalDate startDate,LocalDate endDate){
        User user = getCurrentUser();

        return expenseRepository.findByUserAndDateBetween(user,startDate,endDate).stream()
                .map(this::mapToResponseDTO).toList();
    }

    public Double getMonthlyTotal(int month,int year){
        User user = getCurrentUser();

        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user,startDate,endDate);
        return expenses.stream().mapToDouble(Expense::getAmount).sum();

    }

    public Map<String,Double> getCategoryWiseTotal(){
        List<Expense> expenses = expenseRepository.findByUser(getCurrentUser());
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public List<ExpenseResponseDTO> getAllExpensesSorted(String sortBy){
        List<Expense> expenses = expenseRepository.findByUser(getCurrentUser());

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
                throw new RuntimeException("Invalid sort field: " + sortBy);
        }
        return expenses.stream().map(this::mapToResponseDTO).toList();
    }

    public List<ExpenseResponseDTO> searchByTitle(String title){
        String searchText = title.toLowerCase();
        return expenseRepository.findByUser(getCurrentUser())
                .stream()
                .filter(exp -> exp.getTitle().toLowerCase().contains(searchText))
                .map(this :: mapToResponseDTO)
                .toList();
    }
}

/*
• DTO = what user typed
• entity = what database stores
• service = combines user input + logged-in user + business rules
*/

/*
5.
ExpenseController.java only filters by date if both startDate and endDate exist. If the user sends only one, it silently returns all expenses. Better to throw a clear error or support single-sided filtering.

mvnw.cmd test passes now. So the app starts, but it is not fully logically correct yet. The most important fixes are: return result in search, enforce expense ownership by user, and stop returning User.password in API responses.
 */