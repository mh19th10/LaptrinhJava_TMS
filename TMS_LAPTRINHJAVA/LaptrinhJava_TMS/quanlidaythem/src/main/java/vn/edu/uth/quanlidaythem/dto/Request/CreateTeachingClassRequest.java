package vn.edu.uth.quanlidaythem.dto.Request;

import java.time.LocalDate;

public class CreateTeachingClassRequest {
    private String code;
    private String title;
    private Integer gradeLevel;
    private String subjectCode; // hoặc Long subjectId
    private Long teacherId; // THÊM: teacherId
    private String mode;
    private String location;
    private Double tuition;
    private LocalDate startDate;
    private LocalDate endDate;

    public CreateTeachingClassRequest() {}

    // getters/setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
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
}