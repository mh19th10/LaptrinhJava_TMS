package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;
import vn.edu.uth.quanlidaythem.service.AdminApprovalService;

@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    private final AdminApprovalService approvalService;

    public AdminPermissionController(AdminApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /** Xem toàn bộ permission của 1 giáo viên */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherSubjectPermission>> byTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(approvalService.listPermissionsByTeacher(teacherId));
    }

    /** DUYỆT 1 permission */
    @PostMapping("/{permissionId}/approve")
    public ResponseEntity<TeacherSubjectPermission> approve(@PathVariable Long permissionId) {
        return ResponseEntity.ok(approvalService.approvePermission(permissionId));
    }

    /** TỪ CHỐI 1 permission */
    public static class ReasonDTO { public String reason; }

    @PostMapping("/{permissionId}/reject")
    public ResponseEntity<TeacherSubjectPermission> reject(
            @PathVariable Long permissionId,
            @RequestBody(required = false) ReasonDTO body) {
        return ResponseEntity.ok(approvalService.rejectPermission(permissionId, body != null ? body.reason : null));
    }

    /** DUYỆT tất cả permission của 1 GV */
    @PostMapping("/teacher/{teacherId}/approve-all")
    public ResponseEntity<Integer> approveAll(@PathVariable Long teacherId) {
        return ResponseEntity.ok(approvalService.approveAllByTeacher(teacherId));
    }

    /** TẮT tất cả permission của 1 GV */
    @PostMapping("/teacher/{teacherId}/reject-all")
    public ResponseEntity<Integer> rejectAll(@PathVariable Long teacherId) {
        return ResponseEntity.ok(approvalService.rejectAllByTeacher(teacherId));
    }
}
