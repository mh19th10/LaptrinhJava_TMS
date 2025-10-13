package vn.edu.uth.quanlidaythem.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.edu.uth.quanlidaythem.config.JwtUtils;
import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.LoginRequest;
import vn.edu.uth.quanlidaythem.dto.Request.RegisterRequest;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    // Chuẩn hoá role về EN để khớp SecurityConfig
    private String normalizeRole(String input) {
        if (input == null) return "STUDENT";
        String r = input.trim().toUpperCase();
        switch (r) {
            case "STUDENT":
            case "HOC_SINH":
            case "HỌC SINH":
                return "STUDENT";
            case "TEACHER":
            case "GIAO_VIEN":
            case "GIÁO VIÊN":
                return "TEACHER";
            case "ADMIN":
                return "ADMIN";
            default:
                return "STUDENT";
        }
    }

    /** Đăng ký người dùng */
    public User registerNewUser(RegisterRequest request) {
        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(request.getFullName());
        user.setRole(normalizeRole(request.getRole()));          // ⬅️ quan trọng
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    /** Đăng nhập & tạo JWT */
    public String authenticateAndGenerateToken(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateJwtToken(authentication);
    }
}
