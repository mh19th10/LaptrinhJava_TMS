// src/main/java/vn/edu/uth/quanlidaythem/service/TeacherRegistrationService.java
package vn.edu.uth.quanlidaythem.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.TeacherRegistration;
import vn.edu.uth.quanlidaythem.domain.TeacherRegistration.Status;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;
import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.repository.SubjectRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherRegistrationRepository;
import vn.edu.uth.quanlidaythem.repository.TeachingClassRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class TeacherRegistrationService {

    private final TeacherRegistrationRepository regRepo;
    private final TeachingClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final UserRepository userRepo;

    public TeacherRegistrationService(
            TeacherRegistrationRepository regRepo,
            TeachingClassRepository classRepo,
            SubjectRepository subjectRepo,
            UserRepository userRepo
    ) {
        this.regRepo = regRepo;
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.userRepo = userRepo;
    }

    // lấy user theo username (email login)
    private User getUserByUsernameOrThrow(String username) {
        return userRepo.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user: " + username));
    }

    @Transactional(readOnly = true)
    public List<TeacherRegistration> myRegistrations(String username) {
        User u = getUserByUsernameOrThrow(username);
        return regRepo.findByTeacherIdOrderByIdDesc(u.getId());
    }

    /** Giáo viên đăng ký vào 1 lớp có sẵn */
    @Transactional
    public TeacherRegistration registerIntoClass(String username, Long classId) {
        if (classId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "classId là bắt buộc");
        }
        User u = getUserByUsernameOrThrow(username);
        TeachingClass clazz = classRepo.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp: " + classId));

        TeacherRegistration r = new TeacherRegistration();
        r.setTeacherId(u.getId());
        r.setTeachingClass(clazz);            // gắn class
        r.setStatus(Status.PENDING);
        r.setNote(null);
        // các trường custom để trống
        r.setClassName(null);
        r.setSubjectId(null);
        r.setCapacity(null);
        r.setMode(null);
        r.setStartDate(null);
        r.setLocation(null);
        r.setSchedule(null);
        r.setRequestNote(null);

        return regRepo.save(r);
    }

    /** Giáo viên đề nghị mở lớp mới (không có classId) */
    @Transactional
    public TeacherRegistration registerCustom(
            String username,
            String className,
            Long subjectId,
            Integer capacity,
            String mode,
            LocalDate startDate,
            String location,
            String schedule,
            String note
    ) {
        User u = getUserByUsernameOrThrow(username);

        if (className == null || className.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên lớp là bắt buộc");
        if (subjectId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subjectId là bắt buộc");
        subjectRepo.findById(subjectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy môn: " + subjectId));
        if (schedule == null || schedule.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lịch học là bắt buộc");

        TeacherRegistration r = new TeacherRegistration();
        r.setTeacherId(u.getId());
        r.setStatus(Status.PENDING);
        r.setTeachingClass(null); // QUAN TRỌNG: để null (DB phải cho phép NULL)

        r.setClassName(className);
        r.setSubjectId(subjectId);
        r.setCapacity(capacity);
        r.setMode(mode);
        r.setStartDate(startDate);
        r.setLocation(location);
        r.setSchedule(schedule);
        r.setRequestNote(note);
        r.setNote(null);

        return regRepo.save(r);
    }

    /** Giáo viên hủy yêu cầu khi còn PENDING */
    @Transactional
    public void cancelRegistration(String username, Long regId) {
        User u = getUserByUsernameOrThrow(username);
        TeacherRegistration r = regRepo.findById(regId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký #" + regId));
        if (!u.getId().equals(r.getTeacherId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không sở hữu yêu cầu này");
        if (r.getStatus() != Status.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ hủy khi trạng thái PENDING");

        regRepo.delete(r);
    }
}
