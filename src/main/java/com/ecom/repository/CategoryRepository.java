package com.ecom.repository;

import com.ecom.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    public Boolean existsByCategoryName(String categoryName);

    public List<Category> findByIsActiveTrue();
}
