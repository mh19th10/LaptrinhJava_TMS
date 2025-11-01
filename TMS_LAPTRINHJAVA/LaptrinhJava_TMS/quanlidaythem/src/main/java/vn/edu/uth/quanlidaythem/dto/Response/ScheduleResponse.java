package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.LocalDateTime;

public record ScheduleResponse(
    Long id,             // ID của lịch dạy
    String className,    // Tên lớp học
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location      // Địa điểm lớp học
) {}
