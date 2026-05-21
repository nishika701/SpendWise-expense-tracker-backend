package com.SpendWise.ExpenseTracker.controller;

import com.SpendWise.ExpenseTracker.dto.ExpenseRequestDTO;
import com.SpendWise.ExpenseTracker.dto.ExpenseResponseDTO;
import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private Map<String, Double> totals;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpenseRequestDTO expenseRequestDTO) {
        ExpenseResponseDTO savedExpense = expenseService.createExpense(expenseRequestDTO);
        return new ResponseEntity<ExpenseResponseDTO>(savedExpense, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/{fieldName}")//Because PATCH means “partially update an existing resource.”
    public ResponseEntity<ExpenseResponseDTO> updateOneExpense(@PathVariable Long id, @PathVariable String fieldName, @RequestBody String newValue) {
        ExpenseResponseDTO updateExp = expenseService.updateOneExpense(id, fieldName, newValue);
        return ResponseEntity.ok(updateExp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateFields(@PathVariable Long id, @RequestBody ExpenseRequestDTO expenseRequestDTO) {
        ExpenseResponseDTO updateFields = expenseService.updateFields(id, expenseRequestDTO);
        return ResponseEntity.ok(updateFields);
    }

    @GetMapping("/category")
    public ResponseEntity<List<ExpenseResponseDTO>> filterByCategory(@RequestParam String categoryName) {
        List<ExpenseResponseDTO> filterByCategory = expenseService.filterByCategory(categoryName);
        return new ResponseEntity<>(filterByCategory, HttpStatus.OK);
    }

    @GetMapping
    public List<ExpenseResponseDTO> getExpenses(@RequestParam(required = false) LocalDate startDate,
                                                @RequestParam(required = false) LocalDate endDate,
                                                @RequestParam(required = false) String sortBy) {
        if((startDate == null && endDate != null) || (startDate != null && endDate == null)){
            throw new RuntimeException("Both startDate and endDate are required for filtering");
        }
        if (startDate != null && endDate != null) {
            return expenseService.filterByDateRange(startDate, endDate);
        }
        if (sortBy != null) {
            return expenseService.getAllExpensesSorted(sortBy);
        }
        return expenseService.getAllExpenses();
    }

    @GetMapping("/total/monthly")
    public ResponseEntity<Double> getTotalOfMonthlyExpenses(@RequestParam int month, @RequestParam int year) {
        Double total = expenseService.getMonthlyTotal(month,year);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable Long id) {
        return expenseService.deleteExpense(id);
    }

    @GetMapping("/{id}")
    public ExpenseResponseDTO getExpense(@PathVariable Long id) {
        return expenseService.getExpense(id);
    }

    @GetMapping("/total/category")
    public ResponseEntity<Map<String,Double>> getCategoryWiseTotal(){
        totals = expenseService.getCategoryWiseTotal();
        return ResponseEntity.ok(totals);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseResponseDTO>> searchByTitle(@RequestParam String title){
        List<ExpenseResponseDTO> expenses = expenseService.searchByTitle(title);
        return ResponseEntity.ok(expenses);
    }
}
