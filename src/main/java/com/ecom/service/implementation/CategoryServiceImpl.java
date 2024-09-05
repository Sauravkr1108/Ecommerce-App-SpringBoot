package com.ecom.service.implementation;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.repository.CategoryRepository;
import com.ecom.service.CategoryService;
import com.ecom.service.FileService;
import com.ecom.util.BucketType;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository  categoryRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public Category saveCategory(Category category, MultipartFile file) {
//        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//        category.setImageName(imageName);
        String imageUrl = commonUtil.getImageUrl(file, BucketType.CATEGORY.getId());
        category.setImageName(imageUrl);
        Category savedCategory = categoryRepository.save(category);
        if(!ObjectUtils.isEmpty(savedCategory)) {
//           try {
//                File saveFile = new ClassPathResource("static/static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//               e.printStackTrace();
//           }
           return savedCategory;
        }
        return null;
    }

    @Override
    public Boolean existCategory(String name) {
        return categoryRepository.existsByCategoryName(name);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public Boolean deleteCategory(int id) {
        categoryRepository.deleteById(id);
        return categoryRepository.findById(id).isEmpty();
    }

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category updateCategory(Category category, MultipartFile file) {
        Category oldCategory = getCategoryById(category.getId());
//        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
        oldCategory.setCategoryName(category.getCategoryName());
        oldCategory.setIsActive(category.getIsActive());
//        oldCategory.setImageName(imageName);
        if (!file.isEmpty()) {
            String imageUrl = commonUtil.getImageUrl(file, BucketType.CATEGORY.getId());
            oldCategory.setImageName(imageUrl);
        }
        return categoryRepository.save(oldCategory);
//        if(!ObjectUtils.isEmpty(updateCategory)) {
//            if (!file.isEmpty()) {
//                try {
//                    File saveFile = new ClassPathResource("static/static/img").getFile();
//                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
//                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return updateCategory;
//            }
//        }
    }

    @Override
    public List<Category> getAllActiveCategory() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Category> getAllCategoryPagination(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return categoryRepository.findAll(pageable);
    }
}
