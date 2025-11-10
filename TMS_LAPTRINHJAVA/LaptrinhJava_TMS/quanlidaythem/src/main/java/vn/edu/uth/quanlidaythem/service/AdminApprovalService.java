// vn.edu.uth.quanlidaythem.service.AdminApprovalService
package vn.edu.uth.quanlidaythem.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;

@Service
public class AdminApprovalService {

    private final TeacherSubjectPermissionRepository permRepo;

    public AdminApprovalService(TeacherSubjectPermissionRepository permRepo) {
        this.permRepo = permRepo;
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectPermission> listPermissionsByTeacher(Long teacherId) {
        return permRepo.findByTeacherId(teacherId);
    }

    /** DUYỆT 1 yêu cầu quyền môn -> active=true và xóa note (nếu từng bị từ chối) */
    @Transactional
    public TeacherSubjectPermission approvePermission(Long permissionId) {
        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu: " + permissionId));
        p.setActive(true);
        p.setNote(null); // clear note
        return permRepo.save(p);
    }

    /** TỪ CHỐI 1 yêu cầu quyền môn -> active=false + ghi note (để phân loại REJECTED) */
    @Transactional
    public TeacherSubjectPermission rejectPermission(Long permissionId, String reason) {
        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu: " + permissionId));
        p.setActive(false);
        p.setNote(reason == null || reason.isBlank() ? "Không đạt yêu cầu" : reason);
        return permRepo.save(p);
    }

    /** DUYỆT tất cả quyền môn của 1 GV */
    @Transactional
    public int approveAllByTeacher(Long teacherId) {
        // có thể xóa note hàng loạt nếu cần: lấy list rồi setNote(null) và saveAll
        return permRepo.updateActiveByTeacherId(teacherId, true);
    }

    /** TẮT (reject/suspend) tất cả quyền môn của 1 GV -> active=false (không đặt note) */
    @Transactional
    public int rejectAllByTeacher(Long teacherId) {
        return permRepo.updateActiveByTeacherId(teacherId, false);
    }
}
