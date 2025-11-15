package vn.edu.uth.quanlidaythem.domain;

import java.time.LocalDate;

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

    // ====== Đăng nhập ======
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 30)
    private String role; // ADMIN / TEACHER / STUDENT

    @Column(length = 20)
    private String status;   // ACTIVE / INACTIVE / BANNED


    // ====== Thông tin cơ bản ======
    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String mainSubject;

    // ====== Hồ sơ chi tiết ======
    @Column(name = "dob")
    private LocalDate dob; // ngày sinh

    @Column(length = 100)
    private String degree; // trình độ (Cử nhân, Thạc sĩ, TS, ...)

    @Column
    private Integer experience; // số năm kinh nghiệm

    @Column(length = 255)
    private String address; // địa chỉ

    @Column(length = 1000)
    private String bio; // mô tả bản thân, giới thiệu, thành tích, ...

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

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
