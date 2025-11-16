package vn.edu.uth.quanlidaythem.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.ClassDTO;
import vn.edu.uth.quanlidaythem.dto.DTOMapper;
import vn.edu.uth.quanlidaythem.dto.StudentDTO;
import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.StudentEntity;
import vn.edu.uth.quanlidaythem.model.TeacherEntity;
import vn.edu.uth.quanlidaythem.service.AdminService;

/**
 * Admin Controller - Quản lý lớp học, giáo viên, học sinh
 * Chỉ dành cho ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ============================================
    // QUẢN LÝ GIÁO VIÊN
    // ============================================

    @GetMapping("/teachers/all")
    public ResponseEntity<List<TeacherEntity>> getAllTeachers() {
        return ResponseEntity.ok(adminService.getAllTeachers());
    }


    @PutMapping("/teachers/{id}/status")
    public ResponseEntity<Map<String, Object>> updateTeacherStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        TeacherEntity updated = adminService.updateTeacherStatus(id, status);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("teacher", updated);

        return ResponseEntity.ok(res);
    }


    // ============================================
    // QUẢN LÝ LỚP HỌC
    // ============================================

    /**
     * Lấy tất cả lớp học (có phân trang)
     * GET /api/admin/classes?page=0&size=10
     * Trả về ClassDTO để tránh circular references
     */
    @GetMapping("/classes")
    public ResponseEntity<Page<ClassDTO>> getAllClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type
    ) {
        // Đổi endpoint để tránh trùng
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ClassEntity> classesPage = adminService.getAllClasses(pageable, status, type);
        
        Page<ClassDTO> classDTOPage = classesPage.map(DTOMapper::toClassDTO);
        
        return ResponseEntity.ok(classDTOPage);
    }
    /**
     * Lấy thông tin 1 lớp học theo ID
     * GET /api/admin/classes/{id}
     * Trả về ClassDTO để tránh circular references
     */
    @GetMapping("/classes/{id}")
    public ResponseEntity<ClassDTO> getClassById(@PathVariable Long id) {
        ClassEntity classEntity = adminService.getClassById(id);
        ClassDTO classDTO = DTOMapper.toClassDTO(classEntity);
        return ResponseEntity.ok(classDTO);
    }

    /**
     * Duyệt lớp học (chuyển status sang "approved")
     * PUT /api/admin/classes/{id}/approve
     * Trả về ClassDTO để tránh circular references
     */
    @PutMapping("/classes/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveClass(@PathVariable Long id) {
        ClassEntity approved = adminService.approveClass(id);
        ClassDTO classDTO = DTOMapper.toClassDTO(approved);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Duyệt lớp thành công");
        response.put("data", classDTO);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Từ chối lớp học
     * PUT /api/admin/classes/{id}/reject
     * Trả về ClassDTO để tránh circular references
     */
    @PutMapping("/classes/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectClass(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        ClassEntity rejected = adminService.rejectClass(id, reason);
        ClassDTO classDTO = DTOMapper.toClassDTO(rejected);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã từ chối lớp học");
        response.put("data", classDTO);
        return ResponseEntity.ok(response);
    }


    /**
     * Xóa lớp học
     * DELETE /api/admin/classes/{id}
     */
    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Map<String, Object>> deleteClass(@PathVariable Long id) {
        adminService.deleteClass(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa lớp thành công");
        
        return ResponseEntity.ok(response);
    }

    // ============================================
    // QUẢN LÝ HỌC SINH TRONG LỚP
    // ============================================

    /**
     * Lấy danh sách học sinh của 1 lớp
     * GET /api/admin/classes/{id}/students
     * Trả về StudentDTO để tránh circular references
     */
    @GetMapping("/classes/{id}/students")
    public ResponseEntity<List<StudentDTO>> getStudentsByClass(@PathVariable Long id) {
        List<StudentEntity> students = adminService.getStudentsByClassId(id);
        List<StudentDTO> studentDTOs = DTOMapper.toStudentDTOList(students);
        return ResponseEntity.ok(studentDTOs);
    }

    /**
     * Thêm học sinh vào lớp
     * POST /api/admin/classes/{classId}/students/{studentId}
     */
    @PostMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> addStudentToClass(
            @PathVariable Long classId, 
            @PathVariable Long studentId
    ) {
        adminService.addStudentToClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thêm học sinh vào lớp thành công");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa học sinh khỏi lớp
     * DELETE /api/admin/classes/{classId}/students/{studentId}
     */
    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudentFromClass(
            @PathVariable Long classId, 
            @PathVariable Long studentId
    ) {
        adminService.removeStudentFromClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa học sinh khỏi lớp thành công");
        
        return ResponseEntity.ok(response);
    }

    // ============================================
    // THỐNG KÊ & DASHBOARD
    // ============================================

    /**
     * Thống kê tổng quan
     * GET /api/admin/stats/overview
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = adminService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Thống kê lớp học theo trạng thái
     * GET /api/admin/stats/classes-by-status
     */
    @GetMapping("/stats/classes-by-status")
    public ResponseEntity<Map<String, Long>> getClassesByStatus() {
        Map<String, Long> stats = adminService.getClassesByStatus();
        return ResponseEntity.ok(stats);
    }

    /**
     * Thống kê lớp học theo loại
     * GET /api/admin/stats/classes-by-type
     */
    @GetMapping("/stats/classes-by-type")
    public ResponseEntity<Map<String, Long>> getClassesByType() {
        Map<String, Long> stats = adminService.getClassesByType();
        return ResponseEntity.ok(stats);
    }

    /**
     * Hoạt động gần đây
     * GET /api/admin/stats/recent-activities
     */
    @GetMapping("/stats/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> activities = adminService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    // ============================================
    // HEALTH CHECK
    // ============================================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "OK");
        health.put("service", "Admin API");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}