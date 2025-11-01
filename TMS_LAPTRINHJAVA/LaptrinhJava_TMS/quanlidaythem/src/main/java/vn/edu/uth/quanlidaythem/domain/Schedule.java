package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DayOfWeek enum stored as string for readability
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    // Optional room or note
    private String note;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private TeachingClass teachingClass;

    public Schedule() {}

    public Schedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String note) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public TeachingClass getTeachingClass() { return teachingClass; }
    public void setTeachingClass(TeachingClass teachingClass) { this.teachingClass = teachingClass; }
}
