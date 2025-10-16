package vn.edu.uth.quanlidaythem.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.ChangePasswordRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ChangePasswordResponse; // Response viết hoa R
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String username = a.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

   @Transactional
public ChangePasswordResponse changePassword(ChangePasswordRequest req) {
    User u = currentUser();

    // 1) xác nhận lại mật khẩu mới
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
        return ChangePasswordResponse.fail("Xác nhận mật khẩu không khớp");
    }

    // 2) kiểm tra mật khẩu cũ (raw vs encoded)
    if (!passwordEncoder.matches(req.getOldPassword(), u.getPassword())) {
        return ChangePasswordResponse.fail("Mật khẩu hiện tại không đúng");
    }

    // 3) cập nhật & flush vào DB ngay
    String oldHash = u.getPassword();
    u.setPassword(passwordEncoder.encode(req.getNewPassword()));
    userRepository.saveAndFlush(u);

    System.out.println("\n[DEBUG] Password hash before: " + oldHash);
    System.out.println("[DEBUG] Password hash after : " + u.getPassword() + "\n");

    // ✅ BẮT BUỘC: đăng xuất phiên hiện tại để tránh dùng token/session cũ
    SecurityContextHolder.clearContext();

    return ChangePasswordResponse.ok("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
}

}
