package vn.edu.uth.quanlidaythem.dto.Request;

import jakarta.validation.constraints.*;

public class AddScheduleSlotRequest {
    @NotNull public Long classId;
    @NotNull public String dayOfWeek; // MONDAY..SUNDAY
    @NotBlank public String start; // HH:mm
    @NotBlank public String end;   // HH:mm
    public Long topicId; // optional
}