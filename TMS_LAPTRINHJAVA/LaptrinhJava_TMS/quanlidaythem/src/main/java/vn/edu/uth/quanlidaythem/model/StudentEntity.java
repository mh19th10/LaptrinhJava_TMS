package vn.edu.uth.quanlidaythem.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Student Entity
 * Quản lý thông tin học sinh
 * 
 * QUAN TRỌNG: Phải thêm vào ClassEntity relationship Many-to-Many
 */
@Entity
@Table(name = "students")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    
    @Column(unique = true)
    private String email;
    
    /**
     * Loại học sinh (theo Thông tư 29)
     * - "failed": Học sinh thi rớt môn học kỳ gần nhất
     * - "gifted": Học sinh được chọn bồi dưỡng năng khiếu
     * - "final-year": Học sinh năm cuối tự nguyện ôn thi
     */
    private String studentType;
    
    /**
     * Trạng thái: "active", "inactive"
     */
    private String status;
    
    private String phone;
    private String address;
    private String grade; // Khối lớp: "10", "11", "12"
    
    /**
     * Many-to-Many relationship với Class
     * Một học sinh có thể học nhiều lớp
     * Một lớp có nhiều học sinh
     */
    @ManyToMany
    @JoinTable(
        name = "student_class",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore   // MUST-HAVE
    private List<ClassEntity> classes = new ArrayList<>();


    // ============================================
    // CONSTRUCTORS
    // ============================================
    
    public StudentEntity() {}

    public StudentEntity(String fullName, String email, String studentType) {
        this.fullName = fullName;
        this.email = email;
        this.studentType = studentType;
        this.status = "active";
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public List<ClassEntity> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassEntity> classes) {
        this.classes = classes;
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Thêm lớp học cho học sinh
     */
    public void addClass(ClassEntity classEntity) {
        if (!this.classes.contains(classEntity)) {
            this.classes.add(classEntity);
        }
    }

    /**
     * Xóa lớp học khỏi học sinh
     */
    public void removeClass(ClassEntity classEntity) {
        this.classes.remove(classEntity);
    }

    @Override
    public String toString() {
        return "StudentEntity{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", studentType='" + studentType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}