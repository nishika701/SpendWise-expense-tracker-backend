package com.SpendWise.ExpenseTracker.repository;

import com.SpendWise.ExpenseTracker.model.Expense;
import com.SpendWise.ExpenseTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    public List<Expense> findByUser(User user);
    Optional<Expense> findByIdAndUser(Long id,User user);
    public List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

}
