package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "teaching_subjects")
public class TeachingSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subjectName;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}
