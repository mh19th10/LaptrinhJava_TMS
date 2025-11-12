package vn.edu.uth.quanlidaythem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.Request.LoginRequest;
import vn.edu.uth.quanlidaythem.dto.Request.RegisterRequest;
import vn.edu.uth.quanlidaythem.dto.Response.LoginResponse;
import vn.edu.uth.quanlidaythem.service.AuthService;
import vn.edu.uth.quanlidaythem.service.UserDetailsImpl;

@RestController 
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService; 

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // FIX: Đồng bộ các case để sử dụng tên vai trò chuẩn tiếng Anh
   private String getRedirectUrlByRole(String role) {
    return switch (role) {
        case "ADMIN"   -> "/dashboard_admin.html";              // đúng tên file trong static
        case "TEACHER" -> "/dashboard_teacher.html";  // đúng tên file
        case "STUDENT" -> "/dashboard_student.html";  // đúng tên file
        default -> "/index.html";
    };
}

    // --- API Đăng ký (giữ nguyên) ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.registerNewUser(registerRequest); 
            return ResponseEntity.ok("Đăng ký thành công!"); 
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- API Đăng nhập (logic bên trong không đổi) ---
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateAndGenerateToken(loginRequest);

            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            String redirectUrl = getRedirectUrlByRole(userDetails.getRole());
            
            return ResponseEntity.ok(new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole(),
                redirectUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }
}