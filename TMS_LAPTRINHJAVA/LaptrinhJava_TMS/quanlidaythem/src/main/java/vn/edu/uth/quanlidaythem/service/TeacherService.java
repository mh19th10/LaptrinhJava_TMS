package vn.edu.uth.quanlidaythem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uth.quanlidaythem.domain.Teacher;
import vn.edu.uth.quanlidaythem.domain.TeachingSchedule;
import vn.edu.uth.quanlidaythem.domain.TeachingSubject;
import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.ScheduleRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ScheduleResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherClassItemResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.repository.TeacherRepository;
import vn.edu.uth.quanlidaythem.repository.TeachingScheduleRepository;
import vn.edu.uth.quanlidaythem.repository.TeachingSubjectRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final TeachingSubjectRepository subjectRepository;
    private final TeachingScheduleRepository scheduleRepository;

    public TeacherService(UserRepository userRepository,
                          TeacherRepository teacherRepository,
                          TeachingSubjectRepository subjectRepository,
                          TeachingScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // ================= User/Teacher Info =================
    public UserInfoResponse getInfo(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserInfoResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
    }

    // Lấy danh sách lớp dạy của giáo viên (mock)
    public List<TeacherClassItemResponse> getTeachingClasses(String username) {
        List<TeacherClassItemResponse> list = new ArrayList<>();
        list.add(new TeacherClassItemResponse(
                1L, "Toán 9 - Nâng cao", "Thứ 4 - 18h00", 20,
                "Thứ 4 - 18h00", "Toán 9 - Nâng cao"));
        list.add(new TeacherClassItemResponse(
                2L, "Vật lý 8", "Thứ 6 - 19h00", 25,
                null, null));
        return list;
    }

    // ================= Admin: Quản lý giáo viên =================
    public TeacherResponse registerTeacher(Teacher teacher) {
        teacher.setApproved(false);
        Teacher saved = teacherRepository.save(teacher);
        return mapToResponse(saved);
    }

    public TeacherResponse approveTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        teacher.setApproved(true);
        return mapToResponse(teacher);
    }

    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse addSubject(Long teacherId, String subjectName) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        TeachingSubject subject = new TeachingSubject();
        subject.setSubjectName(subjectName);
        subject.setTeacher(teacher);
        teacher.getSubjects().add(subject);
        subjectRepository.save(subject);
        return mapToResponse(teacher);
    }

    public TeacherResponse addSchedule(Long teacherId, ScheduleRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        TeachingSchedule schedule = new TeachingSchedule();
        schedule.setTeacher(teacher);
        schedule.setClassName(request.className());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setLocation(request.location());
        teacher.getSchedules().add(schedule);
        scheduleRepository.save(schedule);
        return mapToResponse(teacher);
    }

    // ================= Mapping =================
    private TeacherResponse mapToResponse(Teacher teacher) {
        List<String> subjects = teacher.getSubjects() != null ?
                teacher.getSubjects().stream()
                        .map(TeachingSubject::getSubjectName).toList() : List.of();

        List<ScheduleResponse> schedules = teacher.getSchedules() != null ?
                teacher.getSchedules().stream()
                        .map(s -> new ScheduleResponse(
                                s.getId(),
                                s.getClassName(),
                                s.getStartTime(),
                                s.getEndTime(),
                                s.getLocation()))
                        .toList() : List.of();

        return new TeacherResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getPhone(),
                teacher.isApproved(),
                subjects,
                schedules
        );
    }
}
