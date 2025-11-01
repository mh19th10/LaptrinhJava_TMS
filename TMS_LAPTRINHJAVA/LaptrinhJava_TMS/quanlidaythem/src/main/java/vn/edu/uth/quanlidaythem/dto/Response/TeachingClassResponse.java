package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TeachingClassResponse {
    private Long id;
    private String code;
    private String title;
    private Integer gradeLevel;
    private String subjectName;
    private String teacherName;
    private String mode;
    private String location;
    private Double tuition;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Map<String, Object>> schedules;

    // Constructor từ TeachingClass entity
    public static TeachingClassResponse fromEntity(Object entity) {
        if (!(entity instanceof vn.edu.uth.quanlidaythem.domain.TeachingClass teachingClass)) {
            throw new IllegalArgumentException("Entity must be TeachingClass");
        }
        
        TeachingClassResponse response = new TeachingClassResponse();
        response.setId(teachingClass.getId());
        response.setCode(teachingClass.getCode());
        response.setTitle(teachingClass.getTitle()); // DÙNG getTitle() thay vì getClassName()
        response.setGradeLevel(teachingClass.getGradeLevel());
        response.setSubjectName(teachingClass.getSubject() != null ? teachingClass.getSubject().getName() : null);
        response.setTeacherName(teachingClass.getTeacher() != null ? teachingClass.getTeacher().getFullName() : null);
        response.setMode(teachingClass.getMode());
        response.setLocation(teachingClass.getLocation());
        response.setTuition(teachingClass.getTuition());
        response.setStatus(teachingClass.getStatus());
        response.setStartDate(teachingClass.getStartDate());
        response.setEndDate(teachingClass.getEndDate());
        
        // Xử lý schedules - convert từ List<Schedule> sang List<Map>
        if (teachingClass.getSchedules() != null) {
            List<Map<String, Object>> scheduleList = teachingClass.getSchedules().stream()
                    .map(schedule -> {
                        Map<String, Object> scheduleMap = new java.util.HashMap<>();
                        scheduleMap.put("id", schedule.getId());
                        scheduleMap.put("dayOfWeek", schedule.getDayOfWeek());
                        scheduleMap.put("startTime", schedule.getStartTime());
                        scheduleMap.put("endTime", schedule.getEndTime());
                        scheduleMap.put("note", schedule.getNote());
                        return scheduleMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
            response.setSchedules(scheduleList);
        }
        
        return response;
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<Map<String, Object>> getSchedules() { return schedules; }
    public void setSchedules(List<Map<String, Object>> schedules) { this.schedules = schedules; }

    // THÊM CÁC METHODS TƯƠNG ĐƯƠNG MÀ CODE ĐANG GỌI
    public String getClassName() {
        return this.title; // Map title thành className
    }
    
    public String getSchedule() {
        if (this.schedules == null || this.schedules.isEmpty()) {
            return "Chưa có lịch học";
        }
        // Format schedule info
        return this.schedules.stream()
                .map(schedule -> {
                    String day = schedule.get("dayOfWeek").toString();
                    String start = schedule.get("startTime").toString();
                    String end = schedule.get("endTime").toString();
                    return day + ": " + start + " - " + end;
                })
                .collect(java.util.stream.Collectors.joining("; "));
    }
    
    public Integer getMaxStudents() {
        return 30; // Giá trị mặc định hoặc logic thật từ database
    }
    
    public Boolean isActive() {
        return "APPROVED".equals(this.status) || "IN_PROGRESS".equals(this.status);
    }
}