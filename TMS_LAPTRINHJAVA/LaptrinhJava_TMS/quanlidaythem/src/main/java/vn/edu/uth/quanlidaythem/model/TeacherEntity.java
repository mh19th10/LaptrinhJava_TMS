package vn.edu.uth.quanlidaythem.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String subject;
    private String status; // "approved" hoặc "pending"

    @OneToMany(mappedBy = "teacher")
    @JsonIgnore // Tránh vòng tham chiếu khi Jackson serialize Teacher -> classes -> teacher -> ...
    private List<ClassEntity> classes;

    public TeacherEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ClassEntity> getClasses() { return classes; }
    public void setClasses(List<ClassEntity> classes) { this.classes = classes; }
}
