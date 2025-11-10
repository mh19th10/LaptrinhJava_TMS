package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.LocalDate;

public class TeacherProfileResponse {
    private boolean success;
    private String message;

    private String fullName;
    private String username;   // email đăng nhập/username
    private String email;
    private String phone;
    private String mainSubject;

    private LocalDate dob;     // yyyy-MM-dd
    private String degree;
    private Integer experience;
    private String address;
    private String bio;

    // ===== Getters/Setters =====
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMainSubject() { return mainSubject; }
    public void setMainSubject(String mainSubject) { this.mainSubject = mainSubject; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
