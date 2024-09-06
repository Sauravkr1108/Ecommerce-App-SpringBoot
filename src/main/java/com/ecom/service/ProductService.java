package com.ecom.service;

import com.ecom.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    Product saveProduct(Product product, MultipartFile image);

    List<Product> getAllProducts();

    Boolean deleteProduct(int id);

    Product getProductById(int id);

    Product updateProduct(Product product, MultipartFile image);

    List<Product> getAllActiveProducts(String category);

    List<Product> searchProduct(String search);

    Page<Product> getAllActiveProductPagination(int pageNo, int pageSize, String category);

    Page<Product> searchProductPagination(int pageNo, int pageSize, String search);

    Page<Product> getAllProductsPagination(int pageNo, int pageSize);

    Page<Product> searchActiveProductPagination(int pageNo, int pageSize, String category, String search);
}
