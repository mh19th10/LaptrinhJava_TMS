package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.*;

public class CreateSubjectRequest {
    @NotBlank @Size(max = 50)
    public String code;
    @NotBlank @Size(max = 120)
    public String name;
    @NotBlank @Size(max = 30)
    public String grade;
}