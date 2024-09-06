package com.ecom.service.implementation;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;
import com.ecom.util.BucketType;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public Product saveProduct(Product product, MultipartFile image) {

//        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        String imageUrl = commonUtil.getImageUrl(image, BucketType.PRODUCT.getId());
        product.setImage(imageUrl);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());
        Product savedProduct = productRepository.save(product);
        if(!ObjectUtils.isEmpty(savedProduct)) {
//            try {
//                File saveFile = new ClassPathResource("static/static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());
//                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return savedProduct;
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(int id) {
        productRepository.deleteById(id);
        return productRepository.findById(id).isEmpty();
    }

    @Override
    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product oldProduct = getProductById(product.getId());
//        String imageName = image.isEmpty() ? oldProduct.getImage() : image.getOriginalFilename();
        oldProduct.setTitle(product.getTitle());
        oldProduct.setDescription(product.getDescription());
        oldProduct.setCategory(product.getCategory());
        oldProduct.setPrice(product.getPrice());
        oldProduct.setStock(product.getStock());
//        oldProduct.setImage(imageName);
        oldProduct.setDiscount(product.getDiscount());
        Double discountedPrice = product.getPrice() * (100-product.getDiscount())/100;
        oldProduct.setDiscountPrice(discountedPrice);
        oldProduct.setIsActive(product.getIsActive());
        if (!image.isEmpty()) {
            String imageUrl = commonUtil.getImageUrl(image, BucketType.PRODUCT.getId());
            oldProduct.setImage(imageUrl);
        }
        return productRepository.save(oldProduct);
//        if(!ObjectUtils.isEmpty(updatedProduct)){
//            if(!image.isEmpty()){
//                try {
//                    File saveFile = new ClassPathResource("static/static/img").getFile();
//                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());
//                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//            return updatedProduct;
//        }
//        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        if(ObjectUtils.isEmpty(category))
            return productRepository.findByIsActiveTrue();
        else
            return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> searchProduct(String search) {
       return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(search, search);
    }

    @Override
    public Page<Product> getAllActiveProductPagination(int pageNo, int pageSize, String category) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> productPage;

        if(ObjectUtils.isEmpty(category))
            productPage = productRepository.findByIsActiveTrue(pageable);
        else
            productPage = productRepository.findByCategory(pageable, category);

        return productPage;
    }

    @Override
    public Page<Product> getAllProductsPagination(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchActiveProductPagination(int pageNo, int pageSize, String category, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> productPage = productRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(pageable, search, search);
        return productPage;
    }

    @Override
    public Page<Product> searchProductPagination(int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(pageable, search, search);
    }
}
