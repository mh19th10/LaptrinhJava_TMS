package vn.edu.uth.quanlidaythem.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    private String status; // "approved", "pending", "rejected"
     private boolean active = true; // active flag

    @OneToMany(mappedBy = "teacher")
    @JsonIgnore // Tránh vòng tham chiếu khi Jackson serialize Teacher -> classes -> teacher -> ...
    private List<ClassEntity> classes;

    public TeacherEntity() {}

    /**
     * Danh sách môn giáo viên có thể dạy, lưu simple dưới dạng ElementCollection
     * Ví dụ: ["math", "physics"]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> subjects = new HashSet<>();

    /**
     * Số giờ tối đa/tuần (optional). Default 20.
     */
    @Column(name = "max_weekly_hours")
    private Integer maxWeeklyHours = 20;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getMaxWeeklyHours() { return maxWeeklyHours; }
    public void setMaxWeeklyHours(Integer maxWeeklyHours) { this.maxWeeklyHours = maxWeeklyHours; }
    
    public List<ClassEntity> getClasses() { return classes; }
    public void setClasses(List<ClassEntity> classes) { this.classes = classes; }

    // convenience
    public boolean canTeachSubject(String subject) {
        if (subject == null) return false;
        return subjects.stream().anyMatch(s -> s.equalsIgnoreCase(subject));
    }
}
