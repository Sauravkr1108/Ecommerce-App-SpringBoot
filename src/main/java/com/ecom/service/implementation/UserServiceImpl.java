package com.ecom.service.implementation;

import com.ecom.model.User;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;
import com.ecom.util.BucketType;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private CommonUtil commonUtil;

    @Override
    public User saveUser(User user, MultipartFile file) {

        String profileImg = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(profileImg);
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNotLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        String imageUrl = commonUtil.getImageUrl(file, BucketType.PROFILE.getId());
        user.setProfileImage(imageUrl);
        return userRepository.save(user);
//        if(!ObjectUtils.isEmpty(user)) {
//            try {
//                File saveFile = new ClassPathResource("static/static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);    }

    @Override
    public Boolean updateAccountStatus(int id, Boolean status) {

        Optional<User> findByUser = userRepository.findById(id);
        if(findByUser.isPresent()){
            User user = findByUser.get();
            user.setIsEnable(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(User user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(User user) {
        user.setAccountNotLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(User user) {
        long lockTime = user.getLockTime().getTime();
        long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();
        if(currentTime > unlockTime) {
            user.setAccountNotLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(String email) {
        User user = userRepository.findByEmail(email);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        User user = userRepository.findByEmail(email);
        user.setResetToken(resetToken);
        userRepository.save(user);
    }

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUserProfile(User user, MultipartFile img) throws IOException {

        User updateUser = userRepository.findById(user.getId()).get();
        updateUser.setUsername(user.getUsername());
        updateUser.setMobileNo(user.getMobileNo());
        updateUser.setAddress(user.getAddress());
        updateUser.setCity(user.getCity());
        updateUser.setPincode(user.getPincode());

        if(!img.isEmpty()){
//            updateUser.setProfileImage(img.getOriginalFilename());
//            File saveFile = new ClassPathResource("static/static/img").getFile();
//            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + img.getOriginalFilename());
//            Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            String imageUrl = commonUtil.getImageUrl(img, BucketType.PROFILE.getId());
            updateUser.setProfileImage(imageUrl);
        }
        return userRepository.save(updateUser);
    }

    @Override
    public User saveAdmin(User user, MultipartFile file) {
//        String profileImg = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//        user.setProfileImage(profileImg);
        user.setRole("ROLE_ADMIN");
        user.setIsEnable(true);
        user.setAccountNotLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        String imageUrl = commonUtil.getImageUrl(file, BucketType.PROFILE.getId());
        user.setProfileImage(imageUrl);
        return userRepository.save(user);
//        if(!ObjectUtils.isEmpty(user)) {
//            try {
//                File saveFile = new ClassPathResource("static/static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return savedUser;
//        }
//        return null;
    }

    @Override
    public Boolean existEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
