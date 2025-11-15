package vn.edu.uth.quanlidaythem.dto;

/**
 * DTO cho TeacherEntity - không chứa classes để tránh circular references
 */
public class TeacherDTO {
    private Long id;
    private String fullName;
    private String subject;
    private String status;

    public TeacherDTO() {}

    public TeacherDTO(Long id, String fullName, String subject, String status) {
        this.id = id;
        this.fullName = fullName;
        this.subject = subject;
        this.status = status;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TeacherDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
