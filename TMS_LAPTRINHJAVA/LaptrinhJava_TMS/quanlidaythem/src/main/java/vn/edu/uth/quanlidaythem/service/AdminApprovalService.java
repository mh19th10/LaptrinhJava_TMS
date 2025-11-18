// src/main/java/vn/edu/uth/quanlidaythem/service/AdminApprovalService.java
package vn.edu.uth.quanlidaythem.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.TeacherRegistration;
import vn.edu.uth.quanlidaythem.domain.TeacherRegistration.Status;
import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;
import vn.edu.uth.quanlidaythem.repository.ClassScheduleSlotRepository;
import vn.edu.uth.quanlidaythem.repository.SchoolProgressRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherRegistrationRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;
import vn.edu.uth.quanlidaythem.repository.TeachingClassRepository;

@Service
public class AdminApprovalService {

    private final TeacherRegistrationRepository regRepo;
    private final TeachingClassRepository classRepo;
    private final TeacherSubjectPermissionRepository permRepo;
    private final SchoolProgressRepository progressRepo;
    private final ClassScheduleSlotRepository slotRepo;

    public AdminApprovalService(
            TeacherRegistrationRepository regRepo,
            TeachingClassRepository classRepo,
            TeacherSubjectPermissionRepository permRepo,
            SchoolProgressRepository progressRepo,
            ClassScheduleSlotRepository slotRepo
    ) {
        this.regRepo = regRepo;
        this.classRepo = classRepo;
        this.permRepo = permRepo;
        this.progressRepo = progressRepo;
        this.slotRepo = slotRepo;
    }

    // ========== A) Đăng ký LỚP ==========
    
    /** Lấy danh sách đăng ký chờ duyệt (custom registration - chưa gắn lớp) */
    @Transactional(readOnly = true)
    public List<TeacherRegistration> listPendingCustomRegistrations() {
        return regRepo.findByStatusAndTeachingClassIsNullOrderByIdDesc(Status.PENDING);
    }
    
    /** Lấy danh sách đăng ký của một giáo viên */
    @Transactional(readOnly = true)
    public List<TeacherRegistration> getRegistrationsByTeacher(Long teacherId) {
        return regRepo.findByTeacherIdOrderByIdDesc(teacherId);
    }
    
    @Transactional
    public void approveClassRegistration(Long adminId, Long regId) {
        TeacherRegistration reg = regRepo.findById(regId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký #" + regId));
        if (reg.getStatus() != Status.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ duyệt khi trạng thái PENDING");

        TeachingClass clazz = reg.getTeachingClass();
        if (clazz == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đăng ký này chưa gắn lớp cụ thể");

        // (Tuỳ chọn) validate TT29 nếu có
        progressRepo.findBySubject_Id(clazz.getSubject().getId()).ifPresent(progress -> {
            Integer maxWeekAllowed = progress.getMaxWeekAllowed();
            int allowedWeek = (maxWeekAllowed != null) ? maxWeekAllowed : 0;
            slotRepo.findByTeachingClass_Id(clazz.getId()).forEach(slot -> {
                if (slot.getTopic() != null) {
                    Integer weekIdx = slot.getTopic().getWeekIndex();
                    if (weekIdx != null && weekIdx > allowedWeek) {
                        throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Lịch có topic vượt quá tiến độ (week=" + weekIdx + " > " + allowedWeek + ")"
                        );
                    }
                }
            });
        });

        // gắn giáo viên
        clazz.setTeacherId(reg.getTeacherId());
        classRepo.save(clazz);

        reg.setStatus(Status.APPROVED);
        reg.setNote("Admin #" + adminId + " duyệt");
        regRepo.save(reg);
    }

    @Transactional
    public void rejectClassRegistration(Long adminId, Long regId, String reason) {
        TeacherRegistration reg = regRepo.findById(regId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký #" + regId));
        if (reg.getStatus() != Status.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ từ chối khi trạng thái PENDING");

        reg.setStatus(Status.REJECTED);
        reg.setNote("Admin #" + adminId + " từ chối: " + ((reason == null || reason.isBlank()) ? "Không đạt yêu cầu" : reason));
        regRepo.save(reg);
    }

    /** Duyệt một đăng ký "custom" -> tạo lớp mới và gắn, đồng thời tự động duyệt quyền giáo viên dạy môn đó */
    @Transactional
    public void approveCustomRegistration(Long adminId, Long regId, String className, Long subjectId, Integer capacity) {
        TeacherRegistration reg = regRepo.findById(regId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký #" + regId));
        if (reg.getStatus() != Status.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ duyệt khi PENDING");
        if (reg.getTeachingClass() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đăng ký này đã gắn lớp");

        // Xác định subjectId cuối cùng
        Long finalSubjectId = subjectId != null ? subjectId : reg.getSubjectId();
        if (finalSubjectId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có thông tin môn học");
        }

        // Tự động duyệt quyền giáo viên dạy môn này (nếu chưa được duyệt)
        List<TeacherSubjectPermission> perms = permRepo.findByTeacher_Id(reg.getTeacherId());
        TeacherSubjectPermission perm = perms.stream()
                .filter(p -> p.getSubject().getId().equals(finalSubjectId))
                .findFirst()
                .orElse(null);
        
        if (perm != null && !perm.isActive()) {
            // Nếu có permission nhưng chưa được duyệt, tự động duyệt
            perm.setActive(true);
            perm.setNote(null);
            permRepo.save(perm);
        } else if (perm == null) {
            // Nếu chưa có permission, tạo mới và duyệt luôn
            perm = new TeacherSubjectPermission();
            var teacher = new vn.edu.uth.quanlidaythem.domain.User();
            teacher.setId(reg.getTeacherId());
            perm.setTeacher(teacher);
            var subj = new vn.edu.uth.quanlidaythem.domain.Subject();
            subj.setId(finalSubjectId);
            perm.setSubject(subj);
            perm.setActive(true);
            perm.setNote(null);
            permRepo.save(perm);
        }

        // Tạo lớp mới
        TeachingClass clazz = new TeachingClass();
        clazz.setName((className != null && !className.isBlank()) ? className : (reg.getClassName() != null ? reg.getClassName() : ("Lớp từ yêu cầu #" + reg.getId())));
        clazz.setCapacity(capacity != null ? capacity : (reg.getCapacity() != null ? reg.getCapacity() : 30));
        clazz.setTeacherId(reg.getTeacherId());
        
        var subj = new vn.edu.uth.quanlidaythem.domain.Subject();
        subj.setId(finalSubjectId);
        clazz.setSubject(subj);

        classRepo.save(clazz);

        reg.setTeachingClass(clazz);
        reg.setStatus(Status.APPROVED);
        reg.setNote("Admin #" + adminId + " duyệt & tạo lớp mới: #" + clazz.getId());
        regRepo.save(reg);
    }

    // ========== B) Quyền MÔN ==========
    @Transactional(readOnly = true)
    public List<TeacherSubjectPermission> listPermissionsByTeacher(Long teacherId) {
        return permRepo.findByTeacher_Id(teacherId);
    }

    @Transactional
    public TeacherSubjectPermission approvePermission(Long permissionId) {
        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu quyền môn #" + permissionId));
        p.setActive(true);
        p.setNote(null);
        return permRepo.save(p);
    }

    @Transactional
    public TeacherSubjectPermission rejectPermission(Long permissionId, String reason) {
        TeacherSubjectPermission p = permRepo.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu quyền môn #" + permissionId));
        p.setActive(false);
        p.setNote((reason == null || reason.isBlank()) ? "Không đạt yêu cầu" : reason);
        return permRepo.save(p);
    }

    @Transactional
    public int approveAllByTeacher(Long teacherId) {
        return permRepo.updateActiveByTeacherId(teacherId, true);
    }

    @Transactional
    public int rejectAllByTeacher(Long teacherId) {
        return permRepo.updateActiveByTeacherId(teacherId, false);
    }
}
