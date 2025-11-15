package vn.edu.uth.quanlidaythem.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho ClassEntity - không chứa các relationships sinh ra circular refs
 * Thay vào đó chỉ chứa các trường cần thiết và DTOs của nested entities
 */
public class ClassDTO {
    private Long id;
    private String className;
    private String subject;
    private String type;           // "in-school" hoặc "out-school"
    private String status;         // "pending", "approved", "active", "rejected"
    private TeacherDTO teacher;
    private List<ScheduleDTO> schedules;
    private List<StudentDTO> students;
    private Integer studentCount;
    
    public ClassDTO() {}

    public ClassDTO(Long id, String className, String subject, String type, String status,
                    TeacherDTO teacher, List<ScheduleDTO> schedules, 
                    List<StudentDTO> students, Integer studentCount) {
        this.id = id;
        this.className = className;
        this.subject = subject;
        this.type = type;
        this.status = status;
        this.teacher = teacher;
        this.schedules = schedules;
        this.students = students;
        this.studentCount = studentCount;
    }

    private String rejectReason;
    public String getRejectReason() {
        return rejectReason;
    }
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TeacherDTO getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherDTO teacher) {
        this.teacher = teacher;
    }

    public List<ScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleDTO> schedules) {
        this.schedules = schedules;
    }

    public List<StudentDTO> getStudents() {
        return students;
    }

    public void setStudents(List<StudentDTO> students) {
        this.students = students;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "ClassDTO{" +
                "id=" + id +
                ", className='" + className + '\'' +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", studentCount=" + studentCount +
                '}';
    }
}
