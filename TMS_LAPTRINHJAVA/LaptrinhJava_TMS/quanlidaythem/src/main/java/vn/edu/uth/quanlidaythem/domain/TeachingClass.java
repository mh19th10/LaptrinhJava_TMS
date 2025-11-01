package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teaching_classes")
public class TeachingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String title;
    private Integer gradeLevel;
    private Double tuition;
    private String mode; // "IN_SCHOOL" or "OUT_OF_SCHOOL"
    private String location;
    private String status; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, IN_PROGRESS, FINISHED

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher; // SỬA: Dùng Teacher thay vì User

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "teachingClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    public TeachingClass() {}

    // Constructor
    public TeachingClass(String code, String title, Integer gradeLevel, Subject subject, Teacher teacher, String mode, String location, Double tuition, LocalDate startDate, LocalDate endDate) {
        this.code = code;
        this.title = title;
        this.gradeLevel = gradeLevel;
        this.subject = subject;
        this.teacher = teacher;
        this.mode = mode;
        this.location = location;
        this.tuition = tuition;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "DRAFT";
    }

    // Getters & Setters (giữ nguyên, chỉ thay User → Teacher)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Teacher getTeacher() { return teacher; } // SỬA
    public void setTeacher(Teacher teacher) { this.teacher = teacher; } // SỬA
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
}