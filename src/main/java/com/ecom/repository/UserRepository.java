package com.ecom.repository;

import com.ecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

    List<User> findByRole(String role);

    User findByResetToken(String token);

    Boolean existsByEmail(String email);
}
