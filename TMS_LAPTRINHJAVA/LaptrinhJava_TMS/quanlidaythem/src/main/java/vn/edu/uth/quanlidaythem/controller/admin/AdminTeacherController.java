package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vn.edu.uth.quanlidaythem.dto.Response.TeacherAdminView;
import vn.edu.uth.quanlidaythem.service.AdminTeacherService;

@RestController
@RequestMapping("/api/admin/teachers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeacherController {

    private final AdminTeacherService service;

    public AdminTeacherController(AdminTeacherService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TeacherAdminView>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String q) {

        List<TeacherAdminView> data = service.list(status, subject, q); // <<< type rõ ràng
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherAdminView> detail(@PathVariable Long id) {
        TeacherAdminView view = service.get(id); // <<< không dùng Map/Object
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TeacherAdminView> approve(@PathVariable Long id) {
        TeacherAdminView view = service.approve(id);
        return ResponseEntity.ok(view);
    }

    public static class ReasonDTO { public String reason; }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TeacherAdminView> reject(@PathVariable Long id,
                                                   @RequestBody(required = false) ReasonDTO body) {
        TeacherAdminView view = service.reject(id, body != null ? body.reason : null);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<TeacherAdminView> suspend(@PathVariable Long id,
                                                    @RequestBody(required = false) ReasonDTO body) {
        TeacherAdminView view = service.suspend(id, body != null ? body.reason : null);
        return ResponseEntity.ok(view);
    }

    // Reset permission về pending (chỉ dùng cho test/development)
    @PostMapping("/{id}/reset-pending")
    public ResponseEntity<TeacherAdminView> resetToPending(@PathVariable Long id) {
        TeacherAdminView view = service.resetToPending(id);
        return ResponseEntity.ok(view);
    }

    // Hủy quyền đã được duyệt
    @PostMapping("/{id}/revoke")
    public ResponseEntity<TeacherAdminView> revoke(@PathVariable Long id) {
        TeacherAdminView view = service.revoke(id);
        return ResponseEntity.ok(view);
    }
}
