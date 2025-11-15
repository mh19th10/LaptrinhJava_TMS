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
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class AdminTeacherService {

    private final UserRepository userRepo;
    private final TeacherSubjectPermissionRepository permRepo;

    public AdminTeacherService(UserRepository userRepo, TeacherSubjectPermissionRepository permRepo) {
        this.userRepo = userRepo;
        this.permRepo = permRepo;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private TeacherAdminView mapToView(User u, List<TeacherSubjectPermission> perms) {
        TeacherAdminView v = new TeacherAdminView();
        v.id          = u.getId();
        v.name        = u.getFullName();
        v.email       = u.getEmail();
        v.phone       = u.getPhone();
        v.dob         = null;      // nếu User chưa có các field này thì để null
        v.degree      = null;
        v.experience  = null;
        v.address     = null;
        v.bio         = null;
        v.mainSubject = u.getMainSubject();

        // Tính trạng thái tổng
        boolean hasApproved = perms.stream().anyMatch(TeacherSubjectPermission::isActive);
        boolean hasRejected = perms.stream().anyMatch(p -> !p.isActive()
                && p.getNote() != null && !p.getNote().isBlank());
        boolean hasPending  = perms.stream().anyMatch(p -> !p.isActive()
                && (p.getNote() == null || p.getNote().isBlank()));

        if (hasApproved) {
            v.status = "APPROVED";
        } else if (hasRejected) {
            v.status = "REJECTED";
        } else if (hasPending) {
            v.status = "PENDING";
        } else {
            v.status = "PENDING";
        }
        v.active = hasApproved;

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

            var perms = permRepo.findByTeacherId(u.getId());
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
        return mapToView(u, permRepo.findByTeacherId(id));
    }

    @Transactional
    public TeacherAdminView approve(Long teacherId) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        permRepo.updateActiveByTeacherId(teacherId, true);
        // (nếu muốn) clear note từng bản ghi:
        // permRepo.findByTeacherId(teacherId).forEach(p -> { p.setNote(null); permRepo.save(p); });
        return mapToView(u, permRepo.findByTeacherId(teacherId));
    }

    @Transactional
    public TeacherAdminView reject(Long teacherId, String reason) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        // set active=false; ghi note cho các perm đang chờ
        var list = permRepo.findByTeacherId(teacherId);
        for (var p : list) {
            p.setActive(false);
            if (p.getNote() == null || p.getNote().isBlank()) {
                p.setNote(reason == null || reason.isBlank() ? "Từ chối bởi admin" : reason);
            }
            permRepo.save(p);
        }
        return mapToView(u, list);
    }

    @Transactional
    public TeacherAdminView suspend(Long teacherId, String reason) {
        User u = userRepo.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên: " + teacherId));
        // tắt toàn bộ (không buộc phải có note)
        permRepo.updateActiveByTeacherId(teacherId, false);
        return mapToView(u, permRepo.findByTeacherId(teacherId));
    }
}
