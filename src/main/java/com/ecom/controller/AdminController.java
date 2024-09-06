package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.User;
import com.ecom.service.*;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    FileService fileService;

    @GetMapping("/")
    public String index(){
        return "admin/index";
    }

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model){
        if(principal != null){
            User user = userService.getUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categories", allActiveCategory);
    }
//    ***PRODUCTS MAPPING*** ///////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/addProduct")
    public String addProduct(Model model){
        List<Category> categoryList = categoryService.getAllCategory();
        model.addAttribute("categories", categoryList);
        return "admin/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) throws IOException {

        Product savedProduct = productService.saveProduct(product, image);
        Boolean bool = fileService.uploadFileS3(image, 2);
        if(!ObjectUtils.isEmpty(savedProduct) && bool)
            session.setAttribute("successMsg", "Product Saved");
        else
            session.setAttribute("errorMsg", "Internal server error");
        return "redirect:/admin/addProduct";
    }

    @GetMapping("/products")
    public String viewProducts(Model model, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "5")int pageSize){

//        model.addAttribute("products", productService.getAllProducts());
        Page<Product> productPage = productService.getAllProductsPagination(pageNo, pageSize);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productsSize", productPage.getContent().size());
        model.addAttribute("pageNo", productPage.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("isFirst", productPage.isFirst());
        model.addAttribute("isLast", productPage.isLast());
        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session){
        Boolean deleteProduct = productService.deleteProduct(id);
        if(deleteProduct)
            session.setAttribute("succDel", "Product deleted successfully");
        else
            session.setAttribute("errDel", "Delete failed due to server error");
        return "redirect:/admin/products";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model model){
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("categories", categoryService.getAllCategory());
        return "admin/edit_product";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) throws IOException {

        if(product.getDiscount() < 0 || product.getDiscount() >100) {
            session.setAttribute("errorDis", "Invalid Discount");
        } else {
            Product updatedProduct = productService.updateProduct(product, image);
            if(!image.isEmpty())
                fileService.uploadFileS3(image, 2);
            if (!ObjectUtils.isEmpty(updatedProduct))
                session.setAttribute("successMsg", "Product Updated");
            else
                session.setAttribute("errorMsg", "Internal server error");
        }
        return "redirect:/admin/editProduct/" + product.getId();
    }

//    ***CATEGORY MAPPING*** ///////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/category")
    public String category(Model model, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "5")int pageSize){
//        model.addAttribute("categories", categoryService.getAllCategory());
        Page<Category> categoryPage = categoryService.getAllCategoryPagination(pageNo, pageSize);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categorySize", categoryPage.getContent().size());
        model.addAttribute("pageNo", categoryPage.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("isFirst", categoryPage.isFirst());
        model.addAttribute("isLast", categoryPage.isLast());
        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        if(categoryService.existCategory(category.getCategoryName()))
            session.setAttribute("errorMsg", "Category already exists");
        else {
            Category savedCategory = categoryService.saveCategory(category, file);
            Boolean bool = fileService.uploadFileS3(file, 1);
            if(!ObjectUtils.isEmpty(savedCategory) && bool)
                session.setAttribute("successMsg", "Category Added");
            else
                session.setAttribute("serverMsg", "Failed to save! Internal server error");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session){
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if(deleteCategory)
            session.setAttribute("succDel", "Category deleted successfully");
        else
            session.setAttribute("errDel", "Delete failed due to server error");
        return "redirect:/admin/category";
    }

    @GetMapping("/editCategory/{id}")
    public String editCategory(@PathVariable int id, Model model){
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        Category updatedCategory = categoryService.updateCategory(category, file);
        if(!file.isEmpty())
            fileService.uploadFileS3(file, 1);
        if(!ObjectUtils.isEmpty(category))
            session.setAttribute("successMsg", "Category update success");
        else
            session.setAttribute("serverMsg", "Update failed");
        return "redirect:/admin/editCategory/" + category.getId();
    }

    @GetMapping("/users")
    public  String getAllUsers(Model model, @RequestParam int type){
        if(type==1)
            model.addAttribute("users", userService.getUsersByRole("ROLE_USER"));
        else
            model.addAttribute("users", userService.getUsersByRole("ROLE_ADMIN"));
        model.addAttribute("userType", type);
        return "/admin/users";
    }

    @GetMapping("/updateStatus")
    public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam int id, @RequestParam int type, HttpSession session){
        Boolean bool = userService.updateAccountStatus(id, status);
        if(bool)
            session.setAttribute("successMsg", "Account status updated");
        else
            session.setAttribute("errorMsg", "Internal server error");
        return "redirect:/admin/users?type="+type;
    }

    @GetMapping("/orders")
    public  String getAllOrders(Model model, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "3")int pageSize){
//        model.addAttribute("orders", orderService.getAllOrders());
        Page<ProductOrder> orderPage = orderService.getAllOrdersPagination(pageNo, pageSize);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderSize", orderPage.getContent().size());
        model.addAttribute("pageNo", orderPage.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("isFirst", orderPage.isFirst());
        model.addAttribute("isLast", orderPage.isLast());

        return "/admin/orders";
    }

    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam int id, @RequestParam int status, HttpSession session) throws MessagingException, UnsupportedEncodingException {
        String currentStatus = null;
        OrderStatus[] values = OrderStatus.values();
        for(OrderStatus orderStatus : values){
            if(orderStatus.getId() == status)
                currentStatus = orderStatus.getStatusName();
        }
        ProductOrder updateOrder = orderService.updateOrderStatus(id, currentStatus);
        commonUtil.sendMailForProductOrder(updateOrder, currentStatus);

        if(!ObjectUtils.isEmpty(updateOrder))
            session.setAttribute("successMsg", "Status updated!");
        else
            session.setAttribute("errorMsg", "Status not updated.");
        return "redirect:/admin/orders";
    }

    @GetMapping("/search-order")
    public String searchOrder(@RequestParam String orderId, Model model, HttpSession session){

        if(orderId.isEmpty())
            getAllOrders(model, 0, 3 );
        else {
            ProductOrder searchOrder = orderService.getOrderByOrderId(orderId.trim());
            if(!ObjectUtils.isEmpty(searchOrder))
                model.addAttribute("orders", searchOrder);
            else
                session.setAttribute("errorMsg", "Incorrect Order Id");
        }
        return "/admin/orders";
    }

    @GetMapping("/search-product")
    public String searchProduct(@RequestParam String search, Model model){
        List<Product> searchProducts = orderService.searchProduct(search);
        model.addAttribute("products", searchProducts);
        model.addAttribute("totalProducts", searchProducts.size());

        return "admin/products";
    }

    @GetMapping("/filter-products")
    public String filterProduct(Model model, @RequestParam(value = "category", defaultValue = "") String category) {
        List<Product> products = productService.getAllActiveProducts(category);
        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("paramValue", category);
        model.addAttribute("totalProducts", products.size());

        return "admin/products";
    }

    @GetMapping("/add-admin")
    public String addAdmin(){
        return "admin/add_admin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute User user, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        User savedUser = userService.saveAdmin(user, file);
        Boolean bool = fileService.uploadFileS3(file, 3);
        if(!ObjectUtils.isEmpty(savedUser) && bool)
            session.setAttribute("successMsg", "Registered successfully !");
        else
            session.setAttribute("serverMsg", "Internal server error");
        return "redirect:/admin/add-admin";
    }

    @GetMapping("/profile")
    public String profile(){
        return "admin/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user, @RequestParam MultipartFile img, HttpSession session) throws IOException {
        User updatedUser = userService.updateUserProfile(user, img);
        if(!img.isEmpty())
            fileService.uploadFileS3(img, 3);
        if(!ObjectUtils.isEmpty(updatedUser))
            session.setAttribute("successMsg", "User Profile Updated");
        else
            session.setAttribute("errorMsg", "Internal Server Error");
        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal principal, HttpSession session){

        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        if(passwordEncoder.matches(currentPassword, user.getPassword())){
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            session.setAttribute("successMsg", "Password changed");
        } else
            session.setAttribute("errorMsg", "Incorrect current password");
        return "redirect:/admin/profile";
    }
}
