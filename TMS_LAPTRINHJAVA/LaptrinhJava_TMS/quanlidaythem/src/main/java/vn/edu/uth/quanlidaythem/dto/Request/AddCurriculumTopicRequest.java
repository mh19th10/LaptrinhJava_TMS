package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.*;

public class AddCurriculumTopicRequest {
    @NotNull public Long subjectId;
    @Min(1) public int weekIndex;
    @NotBlank @Size(max = 200) public String title;
}