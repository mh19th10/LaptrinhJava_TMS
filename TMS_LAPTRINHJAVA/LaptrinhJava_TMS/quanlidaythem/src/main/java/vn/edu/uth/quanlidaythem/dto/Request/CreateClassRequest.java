package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateClassRequest {
    @NotBlank @Size(max=120) public String name;
    @NotNull public Long subjectId;
    @Min(1) public Integer capacity;
}