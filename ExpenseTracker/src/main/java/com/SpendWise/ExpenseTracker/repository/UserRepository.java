package com.SpendWise.ExpenseTracker.repository;

import com.SpendWise.ExpenseTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}

/*
Because JpaRepository gives you common methods like:
•save
•findById
•findAll
•deleteById

But it does not automatically give you:
•findByEmail
 */