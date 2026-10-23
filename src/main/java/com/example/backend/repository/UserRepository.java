package com.example.backend.repository;

import com.example.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmailaddress(
            String emailaddress
    );

    Optional<User> findByEmailaddressIgnoreCase(
            String emailaddress
    );

    Optional<User> findByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailaddress(
            String emailaddress
    );

    boolean existsByEmailaddressIgnoreCase(
            String emailaddress
    );
}