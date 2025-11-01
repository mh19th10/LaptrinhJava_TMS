package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;

    public ScheduleResponse() {}

    public ScheduleResponse(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String note) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
    }

    // getters/setters
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
}
