package vn.edu.uth.quanlidaythem.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.StudentRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherRepository;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private ClassRepository classRepo;

    @Autowired
    private StudentRepository studentRepo;

    // =================== 1) DASHBOARD STATISTICS ===================
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        long pendingTeachers = teacherRepo.countByStatus("pending");
        long activeClasses = classRepo.countByStatus("approved");
        long totalStudents = studentRepo.count();
        long revenue = 0;
        result.put("revenueMonth", revenue);


        result.put("pendingTeachers", pendingTeachers);
        result.put("activeClasses", activeClasses);
        result.put("totalStudents", totalStudents);
        result.put("revenueMonth", revenue);

        return result;
    }

    // =================== 2) RECENT ACTIVITIES ===================
    @GetMapping("/activities")
    public List<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "8") int limit) {

        List<Map<String, Object>> list = new ArrayList<>();

        // Fake demo activity - FE cần dữ liệu mới hiển thị UI
        Map<String, Object> a1 = new HashMap<>();
        a1.put("title", "Giáo viên mới đăng ký tài khoản");
        a1.put("time", LocalDateTime.now().minusHours(1));
        list.add(a1);

        Map<String, Object> a2 = new HashMap<>();
        a2.put("title", "Lớp học mới được tạo");
        a2.put("time", LocalDateTime.now().minusHours(4));
        list.add(a2);

        Map<String, Object> a3 = new HashMap<>();
        a3.put("title", "Hệ thống cập nhật dữ liệu hàng ngày");
        a3.put("time", LocalDateTime.now().minusHours(6));
        list.add(a3);

        // Giới hạn theo số lượng FE yêu cầu
        return list.subList(0, Math.min(limit, list.size()));
    }
}
