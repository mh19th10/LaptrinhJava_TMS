// src/main/java/vn/edu/uth/quanlidaythem/domain/TeacherRegistration.java
package vn.edu.uth.quanlidaythem.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher_registrations")
public class TeacherRegistration {

    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

   
    @ManyToOne(optional = true)
    @JoinColumn(name = "class_id", nullable = true)
    private TeachingClass teachingClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(length = 300)
    private String note;

    // ===== Thông tin đăng ký mở lớp mới (không có classId) =====
    @Column(length = 150)
    private String className;

    @Column(name = "subject_id")
    private Long subjectId;

    private Integer capacity;

    @Column(length = 50)
    private String mode;          // Trong trường / Ngoài trường / Online

    private LocalDate startDate;

    @Column(length = 150)
    private String location;

    @Column(length = 200)
    private String schedule;      // chuỗi tự do

    @Column(length = 300)
    private String requestNote;   // ghi chú học phí/khác từ GV

    // === getters/setters ===
    public Long getId() { return id; }
    public Long getTeacherId() { return teacherId; }
    public TeachingClass getTeachingClass() { return teachingClass; }
    public Status getStatus() { return status; }
    public String getNote() { return note; }
    public String getClassName() { return className; }
    public Long getSubjectId() { return subjectId; }
    public Integer getCapacity() { return capacity; }
    public String getMode() { return mode; }
    public LocalDate getStartDate() { return startDate; }
    public String getLocation() { return location; }
    public String getSchedule() { return schedule; }
    public String getRequestNote() { return requestNote; }

    public void setId(Long id) { this.id = id; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public void setTeachingClass(TeachingClass teachingClass) { this.teachingClass = teachingClass; }
    public void setStatus(Status status) { this.status = status; }
    public void setNote(String note) { this.note = note; }
    public void setClassName(String className) { this.className = className; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setMode(String mode) { this.mode = mode; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setLocation(String location) { this.location = location; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public void setRequestNote(String requestNote) { this.requestNote = requestNote; }
}
