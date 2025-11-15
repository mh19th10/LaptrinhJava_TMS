package vn.edu.uth.quanlidaythem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService; 

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private String getRedirectUrlByRole(String role) {
        String cleanRole = role.replace("ROLE_", "").toUpperCase();
        
        return switch (cleanRole) {
            case "ADMIN" -> "/dashboard_admin.html";
            case "TEACHER" -> "/dashboard_teacher.html";
            case "STUDENT" -> "/dashboard_student.html";
            default -> {
                logger.warn("Unknown role: {}, redirecting to default page", cleanRole);
                yield "/index.html";
            }
        };
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration attempt for user: {}", registerRequest.getUsername());
            
            authService.registerNewUser(registerRequest);
            
            logger.info("Registration successful for user: {}", registerRequest.getUsername());
            
            return ResponseEntity.ok().body(
                new SimpleResponse("Đăng ký thành công! Vui lòng đăng nhập.")
            );
            
        } catch (RuntimeException e) {
            logger.error("Registration failed for user: {} - Error: {}", 
                registerRequest.getUsername(), e.getMessage());
            
            return ResponseEntity.badRequest().body(
                new SimpleResponse(e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsername());
            
            String jwt = authService.authenticateAndGenerateToken(loginRequest);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("Authentication failed for user: {}", loginRequest.getUsername());
                throw new RuntimeException("Xác thực thất bại");
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            String primaryRole = roles.stream()
                .findFirst()
                .orElse("ROLE_STUDENT")
                .replace("ROLE_", "");
            
            String redirectUrl = getRedirectUrlByRole(primaryRole);
            
            logger.info("Login successful - User: {}, Role: {}, Redirect: {}", 
                userDetails.getUsername(), primaryRole, redirectUrl);
            
            LoginResponse response = new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                redirectUrl
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Login failed for user: {} - Error: {}", 
                loginRequest.getUsername(), e.getMessage());
            
            return ResponseEntity.status(401).body(
                new SimpleResponse("Tên đăng nhập hoặc mật khẩu không đúng.")
            );
        }
    }
    
    // ✅ DTO cho response đơn giản
    private static class SimpleResponse {
        private final String message;
        
        public SimpleResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}