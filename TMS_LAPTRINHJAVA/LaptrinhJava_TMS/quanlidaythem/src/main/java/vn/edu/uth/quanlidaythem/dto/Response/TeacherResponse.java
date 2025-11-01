package vn.edu.uth.quanlidaythem.dto.Response;

import java.util.List;

public record TeacherResponse(
    Long id,                     // ID giáo viên
    String fullName,             // Họ tên
    String email,                // Email
    String phone,                // Số điện thoại
    boolean approved,            // Trạng thái phê duyệt
    List<String> subjects,       // Danh sách môn dạy
    List<ScheduleResponse> schedules  // Danh sách lịch dạy
) {}
