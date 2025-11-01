package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teaching_schedules")
public class TeachingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}
