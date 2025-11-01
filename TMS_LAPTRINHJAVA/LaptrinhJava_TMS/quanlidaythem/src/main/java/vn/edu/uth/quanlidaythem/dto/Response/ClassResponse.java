package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;

public class ClassResponse {
    private Long id;
    private String code;
    private String title;
    private Integer gradeLevel;
    private String subjectName;
    private String teacherUsername;
    private String mode;
    private String location;
    private Double tuition;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Map<String, Object>> schedules;

    public ClassResponse() {}

    // THÊM METHOD fromEntity
    public static ClassResponse fromEntity(Object entity) {
        if (entity instanceof TeachingClass teachingClass) {
            ClassResponse response = new ClassResponse();
            response.setId(teachingClass.getId());
            response.setCode(teachingClass.getCode());
            response.setTitle(teachingClass.getTitle());
            response.setGradeLevel(teachingClass.getGradeLevel());
            response.setSubjectName(teachingClass.getSubject() != null ? teachingClass.getSubject().getName() : null);
            response.setTeacherUsername(teachingClass.getTeacher() != null ? teachingClass.getTeacher().getFullName() : null);
            response.setMode(teachingClass.getMode());
            response.setLocation(teachingClass.getLocation());
            response.setTuition(teachingClass.getTuition());
            response.setStatus(teachingClass.getStatus());
            response.setStartDate(teachingClass.getStartDate());
            response.setEndDate(teachingClass.getEndDate());
            // Schedules sẽ được set riêng nếu cần
            return response;
        }
        throw new IllegalArgumentException("Entity must be TeachingClass");
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
    public String getTeacherUsername() { return teacherUsername; }
    public void setTeacherUsername(String teacherUsername) { this.teacherUsername = teacherUsername; }
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
}