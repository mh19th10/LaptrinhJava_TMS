package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập (bắt buộc & không trùng)
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // Mật khẩu (BCrypt)
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String fullName;

    // VD: ADMIN / TEACHER / STUDENT (hoặc ROLE_ADMIN/ROLE_TEACHER/ROLE_STUDENT tuỳ bạn map)
    @Column(length = 30)
    private String role;

    // ====== BỔ SUNG CHO HỒ SƠ ======
    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String mainSubject;

    // ====== Constructors ======
    public User() {}

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public User(String username, String password, String fullName, String role,
                String email, String phone, String mainSubject) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.mainSubject = mainSubject;
    }

    // ====== Getters/Setters ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMainSubject() { return mainSubject; }
    public void setMainSubject(String mainSubject) { this.mainSubject = mainSubject; }
}
