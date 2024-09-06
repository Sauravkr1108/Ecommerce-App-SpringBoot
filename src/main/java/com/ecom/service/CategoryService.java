package com.ecom.service;

import com.ecom.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    Category saveCategory(Category category, MultipartFile file);

    Boolean existCategory(String name);

    List<Category> getAllCategory();

    Boolean deleteCategory(int id);

    Category getCategoryById(int id);

    Category updateCategory(Category category, MultipartFile file);

    List<Category> getAllActiveCategory();

    Page<Category> getAllCategoryPagination(int pageNo, int pageSize);

}
