package vn.edu.uth.quanlidaythem.dto.Request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UpdateTeacherProfileRequest {
    private String fullName;
    private String phone;
    private String mainSubject;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;      // yyyy-MM-dd
    private String degree;      // Cử nhân/ThS/TS...
    private Integer experience; // số năm
    private String address;
    private String bio;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

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
