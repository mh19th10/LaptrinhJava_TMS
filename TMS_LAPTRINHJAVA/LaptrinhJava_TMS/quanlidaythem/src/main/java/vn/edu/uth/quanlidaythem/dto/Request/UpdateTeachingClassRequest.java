package vn.edu.uth.quanlidaythem.dto.Request;

import java.time.LocalDate;

public class UpdateTeachingClassRequest {
    private String title;
    private Integer gradeLevel;
    private Long subjectId; // THÊM
    private Long teacherId; // THÊM
    private String mode;
    private String location;
    private Double tuition;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive; // THÊM

    public UpdateTeachingClassRequest() {}

    // getters/setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public Long getSubjectId() { return subjectId; } // THÊM
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; } // THÊM
    public Long getTeacherId() { return teacherId; } // THÊM
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; } // THÊM
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Boolean getIsActive() { return isActive; } // THÊM
    public void setIsActive(Boolean isActive) { this.isActive = isActive; } // THÊM
}