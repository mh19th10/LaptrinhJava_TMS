package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.*;

public class UpdateSchoolProgressRequest {
    @NotNull public Long subjectId;
    @Min(1) public int maxWeekAllowed;
}