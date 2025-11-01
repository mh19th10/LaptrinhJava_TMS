package vn.edu.uth.quanlidaythem.dto.Request;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleRequest {
    private Long classId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;

    public ScheduleRequest() {}
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
