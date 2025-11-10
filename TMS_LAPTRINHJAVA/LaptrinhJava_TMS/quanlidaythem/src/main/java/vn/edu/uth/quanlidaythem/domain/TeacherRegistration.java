//Đăng ký dạy của giáo viên cho một lớp
package vn.edu.uth.quanlidaythem.domain;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @ManyToOne(optional = false) @JoinColumn(name = "class_id")
    private TeachingClass teachingClass;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(length = 200)
    private String note;

    public TeacherRegistration() {}

    public Long getId() { return id; }
    public Long getTeacherId() { return teacherId; }
    public TeachingClass getTeachingClass() { return teachingClass; }
    public Status getStatus() { return status; }
    public String getNote() { return note; }
    public void setId(Long id) { this.id = id; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public void setTeachingClass(TeachingClass teachingClass) { this.teachingClass = teachingClass; }
    public void setStatus(Status status) { this.status = status; }
    public void setNote(String note) { this.note = note; }
}