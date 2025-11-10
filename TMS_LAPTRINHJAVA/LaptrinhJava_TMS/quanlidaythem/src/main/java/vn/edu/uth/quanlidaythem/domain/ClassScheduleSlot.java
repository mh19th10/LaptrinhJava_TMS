//Lịch dạy theo buổi (thứ, giờ bắt đầu, kết thúc) của một TeachingClass để kiểm TT29 khi duyệt.
package vn.edu.uth.quanlidaythem.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

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
@Table(name = "class_schedule_slots")
public class ClassScheduleSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "class_id")
    private TeachingClass teachingClass;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne @JoinColumn(name = "topic_id")
    private CurriculumTopic topic; // tuỳ chọn

    public ClassScheduleSlot() {}

    public Long getId() { return id; }
    public TeachingClass getTeachingClass() { return teachingClass; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public CurriculumTopic getTopic() { return topic; }
    public void setId(Long id) { this.id = id; }
    public void setTeachingClass(TeachingClass teachingClass) { this.teachingClass = teachingClass; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setTopic(CurriculumTopic topic) { this.topic = topic; }
}