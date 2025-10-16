package vn.edu.uth.quanlidaythem.dto.Request;

public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String role; // Ví dụ: ADMIN, GIAO_VIEN
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

  
}
