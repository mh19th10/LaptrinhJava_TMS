package vn.edu.uth.quanlidaythem.dto.Response;

/**
 * DTO để hiển thị thông tin lớp học cho học sinh
 */
public class ClassView {
    public Long id;
    public String className;
    public String subject;
    public String type;
    public String status;
    public Integer studentCount;
    public String teacherName;
    /**
     * Có thể đăng ký lớp này không
     */
    public Boolean canRegister;
    /**
     * Thông báo lý do không thể đăng ký (nếu canRegister = false)
     */
    public String registrationMessage;
}

