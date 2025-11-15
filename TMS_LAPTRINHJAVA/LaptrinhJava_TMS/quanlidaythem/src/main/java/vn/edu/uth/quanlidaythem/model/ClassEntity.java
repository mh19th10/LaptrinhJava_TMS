package vn.edu.uth.quanlidaythem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;



@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    private String subject;
    private String type; 
    
    @Column(nullable = true)
    private String rejectReason;
    // getter + setter
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    // ====== NEW: Ngày tạo lớp ======
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("created_at")
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Column(nullable = false)
    private String status = "pending";

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private TeacherEntity teacher;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"classEntity"})
    private List<ScheduleEntity> schedules = new ArrayList<>();

    /**
     * ✅ THÊM MỚI: Many-to-Many relationship với Student
     * Một lớp có nhiều học sinh
     * mappedBy: trỏ đến field "classes" trong StudentEntity
     */
    @ManyToMany(mappedBy = "classes")
    private List<StudentEntity> students = new ArrayList<>();

    // --- Constructors ---
    public ClassEntity() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public TeacherEntity getTeacher() { return teacher; }
    public void setTeacher(TeacherEntity teacher) { this.teacher = teacher; }

    public List<ScheduleEntity> getSchedules() { return schedules; }
    public void setSchedules(List<ScheduleEntity> schedules) { this.schedules = schedules; }

    /**
     * ✅ THÊM MỚI: Getters & Setters cho students
     */
    public List<StudentEntity> getStudents() { 
        return students; 
    }
    
    public void setStudents(List<StudentEntity> students) { 
        this.students = students; 
    }

    // --- Helper Methods ---
    
    /**
     * Thêm lịch học
     */
    public void addSchedule(ScheduleEntity schedule) {
        schedules.add(schedule);
        schedule.setClassEntity(this);
    }

    /**
     * Xóa lịch học
     */
    public void removeSchedule(ScheduleEntity schedule) {
        schedules.remove(schedule);
        schedule.setClassEntity(null);
    }

    /**
     * ✅ THÊM MỚI: Thêm học sinh
     */
    public void addStudent(StudentEntity student) {
        if (!students.contains(student)) {
            students.add(student);
            student.getClasses().add(this);
        }
    }

    /**
     * ✅ THÊM MỚI: Xóa học sinh
     */
    public void removeStudent(StudentEntity student) {
        students.remove(student);
        student.getClasses().remove(this);
    }

    /**
     * ✅ THÊM MỚI: Đếm số học sinh
     */
    public int getStudentCount() {
        return students != null ? students.size() : 0;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


    @Override
    public String toString() {
        return "ClassEntity{" +
                "id=" + id +
                ", className='" + className + '\'' +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", studentCount=" + getStudentCount() +
                '}';
    }
}