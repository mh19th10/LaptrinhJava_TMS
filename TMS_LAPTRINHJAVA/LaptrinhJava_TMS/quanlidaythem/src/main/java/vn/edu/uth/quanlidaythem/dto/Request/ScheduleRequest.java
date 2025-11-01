package vn.edu.uth.quanlidaythem.dto.Request;

import java.time.LocalDateTime;

public record ScheduleRequest(
    String className,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location
) {}
