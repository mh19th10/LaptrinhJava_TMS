package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.service.AdminService;

/**
 * Admin Class Controller - Quản lý lớp học cho Admin
 * Sử dụng AdminService để xử lý logic
 */
@RestController
@RequestMapping("/api/admin/class-management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ClassRepository classRepository;

    /**
     * Lấy danh sách lớp học (có phân trang và filter)
     * GET /api/admin/class-management?status=&type=&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "999") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ClassEntity> classesPage = adminService.getAllClasses(pageable, status, type);
        Page<ClassDTO> classDTOPage = classesPage.map(DTOMapper::toClassDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", classDTOPage.getContent());
        response.put("totalElements", classDTOPage.getTotalElements());
        response.put("totalPages", classDTOPage.getTotalPages());
        response.put("currentPage", page);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin chi tiết 1 lớp học
     * GET /api/admin/class-management/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassDTO> detail(@PathVariable Long id) {
        ClassEntity classEntity = adminService.getClassById(id);
        ClassDTO classDTO = DTOMapper.toClassDTO(classEntity);
        return ResponseEntity.ok(classDTO);
    }

    /**
     * Duyệt lớp học
     * POST /api/admin/class-management/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id) {
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
     * POST /api/admin/class-management/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable Long id, 
            @RequestBody(required=false) ReasonDTO body) {
        
        String reason = (body != null && body.reason != null) ? body.reason : null;
        ClassEntity rejected = adminService.rejectClass(id, reason);
        ClassDTO classDTO = DTOMapper.toClassDTO(rejected);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã từ chối lớp học");
        response.put("data", classDTO);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật thông tin lớp học
     * PUT /api/admin/class-management/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> classData) {
        
        ClassEntity classEntity = adminService.getClassById(id);
        
        // Cập nhật các trường có thể chỉnh sửa
        if (classData.containsKey("className")) {
            classEntity.setClassName((String) classData.get("className"));
        }
        if (classData.containsKey("subject")) {
            classEntity.setSubject((String) classData.get("subject"));
        }
        if (classData.containsKey("type")) {
            classEntity.setType((String) classData.get("type"));
        }
        
        // Save lại
        ClassEntity updated = classRepository.save(classEntity);
        ClassDTO classDTO = DTOMapper.toClassDTO(updated);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật lớp học thành công");
        response.put("data", classDTO);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách học sinh đã đăng ký lớp
     * GET /api/admin/class-management/{id}/students
     * Trả về StudentDTO để tránh circular references
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentDTO>> students(@PathVariable Long id) {
        List<StudentEntity> students = adminService.getStudentsByClassId(id);
        List<StudentDTO> studentDTOs = DTOMapper.toStudentDTOList(students);
        return ResponseEntity.ok(studentDTOs);
    }

    /**
     * Thêm học sinh vào lớp (Admin có thể thêm thủ công)
     * POST /api/admin/class-management/{classId}/students/{studentId}
     */
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> addStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        
        adminService.addStudentToClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thêm học sinh vào lớp thành công");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa học sinh khỏi lớp (Admin có thể xóa)
     * DELETE /api/admin/class-management/{classId}/students/{studentId}
     */
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        
        adminService.removeStudentFromClass(classId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa học sinh khỏi lớp thành công");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm kiếm lớp học (tạm thời dùng lại endpoint list với filter)
     * GET /api/admin/class-management/search?q=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClassDTO>> search(
            @RequestParam String q,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String type) {
        
        // Tạm thời dùng list với filter, có thể cải thiện sau với full-text search
        Pageable pageable = PageRequest.of(0, 100, Sort.by("id").descending());
        Page<ClassEntity> classesPage = adminService.getAllClasses(pageable, status, type);
        
        // Filter by keyword
        List<ClassDTO> filtered = classesPage.getContent().stream()
                .filter(c -> {
                    String searchTerm = q.toLowerCase();
                    return (c.getClassName() != null && c.getClassName().toLowerCase().contains(searchTerm)) ||
                           (c.getSubject() != null && c.getSubject().toLowerCase().contains(searchTerm)) ||
                           (c.getTeacher() != null && c.getTeacher().getFullName() != null && 
                            c.getTeacher().getFullName().toLowerCase().contains(searchTerm));
                })
                .map(DTOMapper::toClassDTO)
                .toList();
        
        return ResponseEntity.ok(filtered);
    }

    /**
     * DTO cho request từ chối
     */
    public static class ReasonDTO { 
        public String reason; 
    }
}