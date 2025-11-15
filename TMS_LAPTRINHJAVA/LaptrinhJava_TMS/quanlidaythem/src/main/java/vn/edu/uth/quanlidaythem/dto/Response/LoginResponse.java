package vn.edu.uth.quanlidaythem.dto.Response;

import java.util.List;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String email;        // ✅ THÊM
    private String role;         // Role chính (ADMIN, TEACHER, STUDENT)
    private List<String> roles;  // ✅ THÊM - Danh sách đầy đủ
    private String redirectUrl;

    // ✅ Constructor MỚI (đầy đủ tham số)
    public LoginResponse(String token, Long id, String username, String email, 
                        List<String> roles, String redirectUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        // Tự động lấy role chính (loại bỏ prefix ROLE_)
        this.role = roles != null && !roles.isEmpty() 
            ? roles.get(0).replace("ROLE_", "") 
            : "STUDENT";
        this.redirectUrl = redirectUrl;
    }

    // ✅ Constructor CŨ (để tương thích với code cũ)
    public LoginResponse(String token, Long id, String username, String role, String redirectUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.role = role;
        this.redirectUrl = redirectUrl;
    }

    // ========== GETTERS & SETTERS ==========
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}