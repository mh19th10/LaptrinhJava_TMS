package vn.edu.uth.quanlidaythem.dto.Request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateStudentProfileRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Email
    @Size(max = 120)
    private String email;

    /**
     * Loại học sinh (theo Thông tư 29)
     * - "failed": Học sinh thi rớt môn học kỳ gần nhất
     * - "gifted": Học sinh được chọn bồi dưỡng năng khiếu
     * - "final-year": Học sinh năm cuối tự nguyện ôn thi
     * null hoặc empty: học sinh thông thường (chỉ đăng ký lớp ngoài trường)
     */
    private String studentType;

    private String phone;
    private String address;
    private String grade; // Khối lớp: "10", "11", "12"

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentType() { return studentType; }
    public void setStudentType(String studentType) { this.studentType = studentType; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
