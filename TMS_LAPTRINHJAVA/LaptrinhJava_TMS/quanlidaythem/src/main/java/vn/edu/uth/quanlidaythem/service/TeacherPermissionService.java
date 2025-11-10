package vn.edu.uth.quanlidaythem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;
import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherPermissionView;
import vn.edu.uth.quanlidaythem.repository.SubjectRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class TeacherPermissionService {

    private final TeacherSubjectPermissionRepository permRepo;
    private final SubjectRepository subjectRepo;
    private final UserRepository userRepo;

    public TeacherPermissionService(TeacherSubjectPermissionRepository permRepo,
                                    SubjectRepository subjectRepo,
                                    UserRepository userRepo) {
        this.permRepo = permRepo;
        this.subjectRepo = subjectRepo;
        this.userRepo = userRepo;
    }

    // ====== Teacher side ======
    @Transactional(readOnly = true)
    public List<TeacherPermissionView> myPermissions(String username) {
        Long teacherId = userRepo.findByUsername(username)
                .map(User::getId).orElseThrow(() -> notFound("Không tìm thấy người dùng: " + username));
        return permRepo.findByTeacherId(teacherId).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    /** GV gửi yêu cầu đăng ký 1 môn (active=false nếu chưa có) */
    @Transactional
    public TeacherPermissionView teacherRequest(String username, Long subjectId) {
        User t = userRepo.findByUsername(username)
                .orElseThrow(() -> notFound("Không tìm thấy người dùng: " + username));
        Subject s = subjectRepo.findById(subjectId)
                .orElseThrow(() -> notFound("Không tìm thấy môn: " + subjectId));

        if (permRepo.existsByTeacherIdAndSubject_IdAndActiveTrue(t.getId(), s.getId())) {
            throw badRequest("Bạn đã được cấp quyền môn này.");
        }

        TeacherSubjectPermission p = permRepo.findByTeacherId(t.getId()).stream()
                .filter(x -> x.getSubject().getId().equals(s.getId()))
                .findFirst()
                .orElseGet(() -> {
                    TeacherSubjectPermission np = new TeacherSubjectPermission();
                    np.setTeacherId(t.getId());
                    np.setSubject(s);
                    np.setActive(false); // pending
                    return np;
                });

        p = permRepo.save(p);
        return toViewWithTeacher(p, t);
    }

    /** GV hủy yêu cầu pending */
    @Transactional
    public void teacherCancel(String username, Long permissionId) {
        User t = userRepo.findByUsername(username)
                .orElseThrow(() -> notFound("Không tìm thấy người dùng: " + username));
        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> notFound("Không tìm thấy quyền môn: " + permissionId));

        if (!p.getTeacherId().equals(t.getId())) {
            throw forbidden("Không thể hủy yêu cầu của người khác.");
        }
        if (p.isActive()) {
            throw badRequest("Quyền đã được duyệt, không thể hủy.");
        }
        permRepo.deleteById(permissionId);
    }

    // ====== mapping ======
    private TeacherPermissionView toView(TeacherSubjectPermission p) {
        TeacherPermissionView v = new TeacherPermissionView();
        v.id = p.getId();
        v.teacherId = p.getTeacherId();
        if (p.getSubject() != null) {
            v.subjectId = p.getSubject().getId();
            v.subjectCode = p.getSubject().getCode();
            v.subjectName = p.getSubject().getName();
            v.subjectGrade = p.getSubject().getGrade();
        }
        v.active = p.isActive();
        userRepo.findById(p.getTeacherId()).ifPresent(u -> {
            v.teacherName = u.getFullName();
            v.teacherEmail = u.getEmail();
        });
        return v;
    }

    private TeacherPermissionView toViewWithTeacher(TeacherSubjectPermission p, User teacher) {
        TeacherPermissionView v = toView(p);
        v.teacherName = teacher.getFullName();
        v.teacherEmail = teacher.getEmail();
        return v;
    }

    // ====== helpers ======
    private ResponseStatusException notFound(String m)   { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    private ResponseStatusException badRequest(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private ResponseStatusException forbidden(String m)  { return new ResponseStatusException(HttpStatus.FORBIDDEN, m); }
}
