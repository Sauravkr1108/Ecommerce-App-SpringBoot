package com.ecom.controller;

import com.ecom.model.*;
import com.ecom.service.*;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public String home() {
        return "/user/home";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam int pid, @RequestParam int uid, HttpSession session){
        Cart savedCart = cartService.saveCart(pid, uid);
        if(ObjectUtils.isEmpty(savedCart))
            session.setAttribute("errorMsg", "Product add to cart failed");
        else
            session.setAttribute("successMsg", "Product added to cart");
        return "redirect:/product/"+pid;
    }

    @GetMapping("/cart")
    public String viewCart(Principal principal, Model model){
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        List<Cart> carts = cartService.getCartByUser(user.getId());
        model.addAttribute("carts", carts);
        if(!carts.isEmpty())
            model.addAttribute("totalOrderPrice", carts.getLast().getTotalOrderPrice());
        return "/user/cart";
    }

    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String symbol, @RequestParam int cid){
        cartService.updateQuantity(symbol, cid);
        return "redirect:/user/cart";
    }

    @GetMapping("/orders")
    public String orderPage(Principal principal, Model model) {

        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        List<Cart> carts = cartService.getCartByUser(user.getId());
        model.addAttribute("carts", carts);
        if(!carts.isEmpty()){
            model.addAttribute("orderPrice", carts.getLast().getTotalOrderPrice());
            model.addAttribute("totalOrderPrice", carts.getLast().getTotalOrderPrice() * 1.18 + 50);
        }
        return "/user/order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest orderRequest, Principal principal, Model model) throws MessagingException, UnsupportedEncodingException {

        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        orderService.saveOrder(user.getId(), orderRequest);
        cartService.removeCartProducts(user.getId());
        model.addAttribute("Msg", "Order Placed");
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String loadSuccess(){
        return "/user/success";
    }

    @GetMapping("/user-orders")
    public String myOrders(Model model, Principal principal){

        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        List<ProductOrder> orders = orderService.getOrderByUser(user.getId());
        model.addAttribute("orders", orders);
        return "/user/my_orders";
    }

    @GetMapping("/update-status")
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
            session.setAttribute("serverMsg", "Status not updated.");
        return "redirect:/user/user-orders";
    }

    @GetMapping("/profile")
    public String profile(){
        return "user/profile";
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
        return "redirect:/user/profile";
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
        return "redirect:/user/profile";
    }
}

