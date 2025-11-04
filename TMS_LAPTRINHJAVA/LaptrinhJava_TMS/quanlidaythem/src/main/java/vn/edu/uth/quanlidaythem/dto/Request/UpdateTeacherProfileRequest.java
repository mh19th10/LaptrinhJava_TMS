package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateTeacherProfileRequest {
    @Size(max = 100)
    private String fullName;

    @Email @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 50)
    private String mainSubject;

    // getters/setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMainSubject() { return mainSubject; }
    public void setMainSubject(String mainSubject) { this.mainSubject = mainSubject; }
}
