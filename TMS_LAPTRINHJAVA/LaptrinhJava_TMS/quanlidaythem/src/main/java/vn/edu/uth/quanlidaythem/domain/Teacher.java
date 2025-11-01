package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập (liên kết tài khoản)
    @Column(nullable = false, unique = true)
    private String username;

    // Họ tên
    @Column(nullable = false)
    private String fullName;

    private String email;
    private String phone;

    // Trạng thái phê duyệt: PENDING / APPROVED / REJECTED
    private String status;

    // Liên kết đến tài khoản User (để mapping với bảng users)
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Danh sách lớp dạy
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<TeachingClass> classes;

    public Teacher() {}

    // ==== GETTERS & SETTERS ====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<TeachingClass> getClasses() { return classes; }
    public void setClasses(List<TeachingClass> classes) { this.classes = classes; }
}
