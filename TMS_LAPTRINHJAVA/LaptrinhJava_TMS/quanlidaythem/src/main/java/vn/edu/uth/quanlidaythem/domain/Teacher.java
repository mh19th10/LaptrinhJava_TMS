package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;

    private boolean approved; // thêm trường này

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<TeachingSubject> subjects;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<TeachingSchedule> schedules;

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public List<TeachingSubject> getSubjects() { return subjects; }
    public void setSubjects(List<TeachingSubject> subjects) { this.subjects = subjects; }

    public List<TeachingSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<TeachingSchedule> schedules) { this.schedules = schedules; }
}
