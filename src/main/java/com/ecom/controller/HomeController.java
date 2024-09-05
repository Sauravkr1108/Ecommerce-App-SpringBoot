package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.User;
import com.ecom.service.*;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private FileService fileService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserDetails(Principal principal, Model model){
		if(principal != null){
			User user = userService.getUserByEmail(principal.getName());
			model.addAttribute("user", user);
			int countCart = cartService.getCountCart(user.getId());
			model.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		model.addAttribute("categories", allActiveCategory);
	}
	@GetMapping("/")
	public String index(Model model) {

		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
				.limit(4).toList();
		List<Category> allActiveCategories = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
				.limit(6).toList();
		model.addAttribute("products", allActiveProducts);
		model.addAttribute("categories", allActiveCategories);
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model model, @RequestParam(value = "category", defaultValue = "") String category,
						   @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
						   @RequestParam(name = "pageSize", defaultValue = "4") int pageSize,
						   @RequestParam(defaultValue = "") String search) {

//		List<Product> products = productService.getAllActiveProducts(category);
//		model.addAttribute("products", products);
		Page<Product> productPage = null;
		if(search.isEmpty())
			productPage = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		else
			productPage = productService.searchActiveProductPagination(pageNo, pageSize, category, search);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("productsSize", productPage.getContent().size());
		model.addAttribute("pageNo", productPage.getNumber());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalElements", productPage.getTotalElements());
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("isFirst", productPage.isFirst());
		model.addAttribute("isLast", productPage.isLast());

		List<Category> categories = categoryService.getAllActiveCategory();
		model.addAttribute("categories", categories);
		model.addAttribute("paramValue", category);

		return "products";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model model) {
		Product product = productService.getProductById(id);
		model.addAttribute("product", product);
		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute User user, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

		if(userService.existEmail(user.getEmail())){
			session.setAttribute("errorMsg", "Email already exists!");
		}
		else {
			User savedUser = userService.saveUser(user, file);
			Boolean bool = fileService.uploadFileS3(file, 3);
			if(!ObjectUtils.isEmpty(savedUser) && bool)
				session.setAttribute("successMsg", "Registered successfully !");
			else
				session.setAttribute("serverMsg", "Internal server error");
		}
		return "redirect:/register";
	}

	@GetMapping("/forgot-password")
	public String showForgotPassword(){
		return "forgot_password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
		User user = userService.getUserByEmail(email);
		if(!ObjectUtils.isEmpty(user)){
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);
			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
			Boolean sendEmail = commonUtil.sendMail(url, email);
			if(sendEmail)
				session.setAttribute("successMsg", "Please check your email. Password reset link sent");
			else
				session.setAttribute("errorMsg", "Internal server error. Email not sent.");
		} else
			session.setAttribute("errorMsg", "Invalid Email");
		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, Model model){
		User user = userService.getUserByToken(token);
		if(user==null){
			model.addAttribute("Msg", "Your link is invalid or expired");
			return "message";
		} else
			model.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password,  Model model){
		User user = userService.getUserByToken(token);
		user.setPassword(passwordEncoder.encode(password));
		user.setResetToken(null);
		userService.updateUser(user);
		model.addAttribute("Msg", "Password changed successfully");
		return "message";
	}

	@GetMapping("/search")
	public String searchProduct(@RequestParam String search, Model model){
		List<Product> searchProducts = productService.searchProduct(search);
		model.addAttribute("products", searchProducts);
		return "products";
	}
}
