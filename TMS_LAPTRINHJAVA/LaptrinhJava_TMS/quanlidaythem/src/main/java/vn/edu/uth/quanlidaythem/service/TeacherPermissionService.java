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

    public TeacherPermissionService(
            TeacherSubjectPermissionRepository permRepo,
            SubjectRepository subjectRepo,
            UserRepository userRepo
    ) {
        this.permRepo = permRepo;
        this.subjectRepo = subjectRepo;
        this.userRepo = userRepo;
    }

    // ====== Lấy danh sách môn gv đã đăng ký ======
    @Transactional(readOnly = true)
    public List<TeacherPermissionView> myPermissions(String username) {

        User teacher = userRepo.findByUsername(username)
                .orElseThrow(() -> notFound("Không tìm thấy giáo viên: " + username));

        return permRepo.findByTeacher_Id(teacher.getId())
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    // ====== Giáo viên gửi yêu cầu đăng ký môn ======
    @Transactional
    public TeacherPermissionView teacherRequest(String username, Long subjectId) {

        User teacher = userRepo.findByUsername(username)
                .orElseThrow(() -> notFound("Không tìm thấy giáo viên: " + username));

        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> notFound("Không tìm thấy môn: " + subjectId));

        // Ngăn đăng ký trùng sau khi đã được duyệt
        if (permRepo.existsByTeacher_IdAndSubject_IdAndActiveTrue(teacher.getId(), subject.getId())) {
            throw badRequest("Bạn đã được duyệt môn này rồi.");
        }

        // Kiểm tra bản ghi pending có chưa
        TeacherSubjectPermission p = permRepo.findByTeacher_Id(teacher.getId())
                .stream()
                .filter(x -> x.getSubject().getId().equals(subject.getId()))
                .findFirst()
                .orElseGet(() -> {
                    TeacherSubjectPermission np = new TeacherSubjectPermission();
                    np.setTeacher(teacher);      // DÙNG USER — ĐÚNG CHUẨN
                    np.setSubject(subject);
                    np.setActive(false);
                    return np;
                });

        p = permRepo.save(p);
        return toViewWithTeacher(p, teacher);
    }

    // ====== Giáo viên hủy yêu cầu ======
    @Transactional
    public void teacherCancel(String username, Long permissionId) {

        User teacher = userRepo.findByUsername(username)
                .orElseThrow(() -> notFound("Không tìm thấy giáo viên: " + username));

        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> notFound("Không tìm thấy quyền môn: " + permissionId));

        if (!p.getTeacher().getId().equals(teacher.getId())) {
            throw forbidden("Không thể hủy yêu cầu của người khác.");
        }

        if (p.isActive()) {
            throw badRequest("Quyền đã được duyệt, không thể hủy.");
        }

        permRepo.deleteById(permissionId);
    }

    // ====== Mapping DTO ======
    private TeacherPermissionView toView(TeacherSubjectPermission p) {
        TeacherPermissionView v = new TeacherPermissionView();

        v.id = p.getId();
        v.teacherId = p.getTeacher().getId();
        v.active = p.isActive();

        if (p.getSubject() != null) {
            v.subjectId = p.getSubject().getId();
            v.subjectCode = p.getSubject().getCode();
            v.subjectName = p.getSubject().getName();
            v.subjectGrade = p.getSubject().getGrade();
        }

        userRepo.findById(v.teacherId).ifPresent(u -> {
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

    // ====== Helpers ======
    private ResponseStatusException notFound(String m)   { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    private ResponseStatusException badRequest(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private ResponseStatusException forbidden(String m)  { return new ResponseStatusException(HttpStatus.FORBIDDEN, m); }
}
