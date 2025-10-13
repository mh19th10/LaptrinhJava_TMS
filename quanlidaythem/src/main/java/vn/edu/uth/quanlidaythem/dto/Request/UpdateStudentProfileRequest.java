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

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
