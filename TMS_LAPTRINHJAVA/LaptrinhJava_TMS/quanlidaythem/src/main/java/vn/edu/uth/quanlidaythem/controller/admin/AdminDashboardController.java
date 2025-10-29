package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    // GET /api/admin/dashboard/statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("pendingTeachers", 5);
        data.put("activeClasses", 24);
        data.put("totalStudents", 342);
        data.put("monthlyRevenue", 125_000_000);
        return ResponseEntity.ok(data);
    }

    // GET /api/admin/dashboard/activities?limit=10
    @GetMapping("/activities")
    public ResponseEntity<List<Map<String, Object>>> activities(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("title","Giáo viên mới đăng ký","time","5 phút trước","status","PENDING"));
        list.add(Map.of("title","Lớp học được tạo","time","15 phút trước","status","APPROVED"));
        return ResponseEntity.ok(list);
    }

    // GET /api/admin/dashboard/pending
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> pending() {
        // TODO: thay bằng service thật
        return ResponseEntity.ok(Map.of("pendingTeachers", List.of(1,2,3)));
    }
}
