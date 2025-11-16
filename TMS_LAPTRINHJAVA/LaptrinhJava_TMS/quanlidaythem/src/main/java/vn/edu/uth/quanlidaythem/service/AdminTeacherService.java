// vn.edu.uth.quanlidaythem.service.AdminTeacherService
package vn.edu.uth.quanlidaythem.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;
import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherAdminView;
import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class AdminTeacherService {

    private final UserRepository userRepo;
    private final TeacherSubjectPermissionRepository permRepo;
    private final ClassRepository classRepo;

    public AdminTeacherService(UserRepository userRepo, TeacherSubjectPermissionRepository permRepo, ClassRepository classRepo) {
        this.userRepo = userRepo;
        this.permRepo = permRepo;
        this.classRepo = classRepo;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private TeacherAdminView mapToView(User u, List<TeacherSubjectPermission> perms) {
        TeacherAdminView v = new TeacherAdminView();
        v.id          = u.getId();
        v.name        = u.getFullName();
        v.fullName    = u.getFullName();  // << thêm để tương thích với frontend
        v.email       = u.getEmail();
        v.phone       = u.getPhone();
        v.dob         = null;      // nếu User chưa có các field này thì để null
        v.degree      = null;
        v.experience  = null;
        v.address     = null;
        v.bio         = null;
        v.mainSubject = u.getMainSubject();
        
        // Đếm số lớp của giáo viên
        // Thử dùng native query trước (teacher_id có thể trỏ đến users.id)
        // Nếu không được, thử dùng JPA query (teacher_id trỏ đến teachers.id)
        try {
            v.classCount = (int) classRepo.countByTeacherIdNative(u.getId());
        } catch (Exception e) {
            try {
                // Thử dùng JPA query nếu native query không khớp
                v.classCount = (int) classRepo.countByTeacherId(u.getId());
            } catch (Exception e2) {
                // Nếu cả hai đều không khớp, đặt về 0
                v.classCount = 0;
            }
        }

        // Tính trạng thái tổng
        // Ưu tiên: PENDING > REJECTED > APPROVED
        // Nếu có bất kỳ permission nào đang chờ duyệt -> PENDING
        boolean hasPending  = perms.stream().anyMatch(p -> !p.isActive()
                && (p.getNote() == null || p.getNote().isBlank()));
        boolean hasRejected = perms.stream().anyMatch(p -> !p.isActive()
                && p.getNote() != null && !p.getNote().isBlank());
        boolean hasApproved = perms.stream().anyMatch(TeacherSubjectPermission::isActive);

        if (hasPending) {
            // Nếu có permission đang chờ duyệt -> PENDING (ưu tiên cao nhất)
            v.status = "PENDING";
        } else if (hasRejected && !hasApproved) {
            // Nếu chỉ có rejected và không có approved -> REJECTED
            v.status = "REJECTED";
        } else if (hasApproved) {
            // Nếu có approved (và không có pending) -> APPROVED
            v.status = "APPROVED";
        } else {
            // Trường hợp khác (không có pending, rejected, approved) -> PENDING
            // Lưu ý: Trường hợp này không nên xảy ra vì đã filter ra giáo viên không có permission
            v.status = "PENDING";
        }
        v.active = hasApproved && !hasPending; // Chỉ active khi có approved và không có pending

        // Lý do từ chối (nếu có)
        v.rejectReason = perms.stream()
                .filter(p -> !p.isActive() && p.getNote()!=null && !p.getNote().isBlank())
                .map(TeacherSubjectPermission::getNote)
                .findFirst().orElse(null);

        // Danh sách quyền theo từng môn
        List<TeacherAdminView.SubjectPermissionView> items = new ArrayList<>();
        for (TeacherSubjectPermission p : perms) {
            TeacherAdminView.SubjectPermissionView sp = new TeacherAdminView.SubjectPermissionView();
            sp.id        = p.getId();
            sp.subjectId = p.getSubject().getId();
            sp.code      = p.getSubject().getCode();
            sp.name      = p.getSubject().getName();
            sp.grade     = p.getSubject().getGrade();
            sp.active    = p.isActive();
            sp.note      = p.getNote();
            items.add(sp);
        }
        v.subjects = items;
        return v;
    }

    @Transactional(readOnly = true)
    public List<TeacherAdminView> list(String status, String subject, String q) {
        String s   = status  == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String sub = subject == null ? "" : subject.trim().toLowerCase(Locale.ROOT);
        String qq  = q       == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<User> all = userRepo.findAll(); // nếu có findByRole("TEACHER") thì dùng
        List<TeacherAdminView> out = new ArrayList<>();

        for (User u : all) {
            boolean isTeacher = false;
            String r = u.getRole();
            if (r != null) {
                String rr = r.trim().toUpperCase();
                if (rr.equals("TEACHER")) {
                    isTeacher = true;
                }
            }
            if (!isTeacher) continue;

            var perms = permRepo.findByTeacher_Id(u.getId());
            
            // Chỉ hiển thị giáo viên có ít nhất 1 permission (đã đăng ký môn)
            // Nếu chưa đăng ký môn nào, bỏ qua (không hiển thị trong danh sách)
            if (perms.isEmpty()) continue;
            
            var view  = mapToView(u, perms);

            if (!s.isEmpty() && !s.equals(view.status)) continue;

            if (!sub.isEmpty()) {
                boolean match = perms.stream().anyMatch(p ->
                    String.valueOf(p.getSubject().getId()).equals(sub) ||
                    (p.getSubject().getCode() != null && p.getSubject().getCode().equalsIgnoreCase(sub))
                );
                if (!match) continue;
            }

            if (!qq.isEmpty()) {
                String any = (safe(u.getFullName()) + " " + safe(u.getEmail()) + " " +
                              safe(u.getPhone()) + " " + safe(u.getMainSubject()))
                              .toLowerCase(Locale.ROOT);
                if (!any.contains(qq)) continue;
            }

            out.add(view);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public TeacherAdminView get(Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + id));
        return mapToView(u, permRepo.findByTeacher_Id(id));
    }

    @Transactional
    public TeacherAdminView approve(Long teacherId) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        
        // Chỉ duyệt các permission đang pending (active=false, note=null hoặc blank)
        // Không ảnh hưởng đến các permission đã được duyệt trước đó
        List<TeacherSubjectPermission> perms = permRepo.findByTeacher_Id(teacherId);
        boolean hasAnyPending = false;
        for (TeacherSubjectPermission p : perms) {
            // Chỉ duyệt permission đang pending (chưa được duyệt và chưa bị từ chối)
            if (!p.isActive() && (p.getNote() == null || p.getNote().isBlank())) {
                // Chỉ duyệt permission đang pending
                p.setActive(true);
                p.setNote(null);
                permRepo.save(p);
                hasAnyPending = true;
            }
        }
        
        // Nếu không có permission nào đang pending, throw exception
        if (!hasAnyPending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giáo viên này không có yêu cầu nào đang chờ duyệt. Tất cả các môn đã được duyệt hoặc từ chối.");
        }
        
        return mapToView(u, permRepo.findByTeacher_Id(teacherId));
    }

    @Transactional
    public TeacherAdminView reject(Long teacherId, String reason) {

        User u = userRepo.findById(teacherId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy giáo viên: " + teacherId));

        List<TeacherSubjectPermission> list = permRepo.findByTeacher_Id(teacherId);

        // Chỉ từ chối các permission đang pending (active=false, note=null hoặc blank)
        // Không ảnh hưởng đến các permission đã được duyệt trước đó
        boolean hasAnyPending = false;
        for (TeacherSubjectPermission p : list) {
            if (!p.isActive() && (p.getNote() == null || p.getNote().isBlank())) {
                // Chỉ từ chối permission đang pending
                p.setActive(false);
                p.setNote((reason == null || reason.isBlank()) 
                    ? "Từ chối bởi admin" 
                    : reason);
                permRepo.save(p);
                hasAnyPending = true;
            }
        }

        // Nếu không có permission nào đang pending, throw exception
        if (!hasAnyPending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giáo viên này không có yêu cầu nào đang chờ duyệt. Tất cả các môn đã được duyệt hoặc từ chối.");
        }

        return mapToView(u, permRepo.findByTeacher_Id(teacherId));
    }


    // Reset tất cả permission của giáo viên về pending (chỉ dùng cho test/development)
    @Transactional
    public TeacherAdminView resetToPending(Long teacherId) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        
        List<TeacherSubjectPermission> perms = permRepo.findByTeacher_Id(teacherId);
        for (TeacherSubjectPermission p : perms) {
            // Reset về pending: active=false, note=null
            p.setActive(false);
            p.setNote(null);
            permRepo.save(p);
        }
        
        return mapToView(u, permRepo.findByTeacher_Id(teacherId));
    }

    @Transactional
    public TeacherAdminView suspend(Long teacherId, String reason) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        // tắt toàn bộ (không buộc phải có note)
        permRepo.updateActiveByTeacherId(teacherId, false);
        return mapToView(u, permRepo.findByTeacher_Id(teacherId));
    }

    // Hủy quyền đã được duyệt (chỉ hủy các permission active=true)
    @Transactional
    public TeacherAdminView revoke(Long teacherId) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        
        List<TeacherSubjectPermission> perms = permRepo.findByTeacher_Id(teacherId);
        boolean hasAnyApproved = false;
        
        // Chỉ hủy các permission đã được duyệt (active=true)
        for (TeacherSubjectPermission p : perms) {
            if (p.isActive()) {
                // Hủy quyền: set active=false, xóa note (để có thể đăng ký lại sau)
                p.setActive(false);
                p.setNote(null);
                permRepo.save(p);
                hasAnyApproved = true;
            }
        }
        
        // Nếu không có permission nào đã được duyệt, throw exception
        if (!hasAnyApproved) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Giáo viên này không có quyền nào đã được duyệt để hủy.");
        }
        
        return mapToView(u, permRepo.findByTeacher_Id(teacherId));
    }
}
