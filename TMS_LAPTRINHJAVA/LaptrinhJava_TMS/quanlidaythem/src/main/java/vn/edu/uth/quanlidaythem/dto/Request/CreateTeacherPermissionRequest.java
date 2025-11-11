package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.NotNull;

public class CreateTeacherPermissionRequest {
    @NotNull
    public Long subjectId;
}
