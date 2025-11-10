// vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission
package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher_subject_permissions")
public class TeacherSubjectPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="teacher_id", nullable = false)
    private Long teacherId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private boolean active = false;   // được duyệt hay chưa

    @Column(length = 200)
    private String note;              // lý do từ chối (nếu có) => REJECTED

    // ===== getters/setters =====
    public Long getId() { return id; }
    public Long getTeacherId() { return teacherId; }
    public Subject getSubject() { return subject; }
    public boolean isActive() { return active; }
    public String getNote() { return note; }

    public void setId(Long id) { this.id = id; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public void setActive(boolean active) { this.active = active; }
    public void setNote(String note) { this.note = note; }
}
