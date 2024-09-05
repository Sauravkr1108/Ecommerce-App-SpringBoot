package com.ecom.service;

import com.ecom.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    User saveUser(User user, MultipartFile file);

    User getUserByEmail(String email);

    List<User> getUsersByRole(String role);

    Boolean updateAccountStatus(int id, Boolean status);

    void increaseFailedAttempt(User user);

    void userAccountLock(User user);

    Boolean unlockAccountTimeExpired(User user);

    void resetAttempt(String email);

    void updateUserResetToken(String email, String resetToken);

    User getUserByToken(String Token);

    User updateUser(User user);

    User updateUserProfile(User user, MultipartFile img) throws IOException;

    User saveAdmin(User user, MultipartFile file);

    Boolean existEmail(String email);

}
