package vn.edu.uth.quanlidaythem.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.*;
import vn.edu.uth.quanlidaythem.dto.Request.CreateClassRequest;
import vn.edu.uth.quanlidaythem.dto.Request.AssignTeacherRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ClassResponse;
import vn.edu.uth.quanlidaythem.repository.*;

@Service
public class ClassService {

    private final TeachingClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final ScheduleRepository scheduleRepo;

    public ClassService(TeachingClassRepository classRepo,
                      SubjectRepository subjectRepo,
                      TeacherRepository teacherRepo,
                      ScheduleRepository scheduleRepo) {
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.scheduleRepo = scheduleRepo;
    }

    /**
     * Tạo lớp học mới
     */
    @Transactional
    public TeachingClass createClass(CreateClassRequest request) {
        Subject subject = subjectRepo.findByCode(request.getSubjectCode())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + request.getSubjectCode()));

        // Validation
        if (request.getGradeLevel() < subject.getGradeLevel()) {
            throw new IllegalArgumentException("Không được dạy trước chương trình");
        }

        if ("OUT_OF_SCHOOL".equalsIgnoreCase(request.getMode()) && 
            (request.getTuition() == null || request.getTuition() <= 0)) {
            throw new IllegalArgumentException("Lớp ngoài trường phải có học phí hợp lệ");
        }

        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setCode(request.getCode());
        teachingClass.setTitle(request.getTitle());
        teachingClass.setGradeLevel(request.getGradeLevel());
        teachingClass.setSubject(subject);
        teachingClass.setMode(request.getMode());
        teachingClass.setLocation(request.getLocation());
        teachingClass.setTuition(request.getTuition());
        teachingClass.setStatus("DRAFT");
        teachingClass.setStartDate(request.getStartDate());
        teachingClass.setEndDate(request.getEndDate());

        return classRepo.save(teachingClass);
    }

    /**
     * Lấy tất cả lớp học
     */
    public List<ClassResponse> getAllClasses() {
        return classRepo.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lớp học theo ID
     */
    public TeachingClass getClassById(Long id) {
        return classRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Class not found with id: " + id));
    }

    /**
     * Phân công giáo viên
     */
    @Transactional
    public TeachingClass assignTeacher(AssignTeacherRequest request) {
        TeachingClass teachingClass = classRepo.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        Teacher teacher = teacherRepo.findById(request.getTeacherUserId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        teachingClass.setTeacher(teacher);
        teachingClass.setStatus("PENDING_APPROVAL");

        return classRepo.save(teachingClass);
    }

    /**
     * Gửi phê duyệt
     */
    @Transactional
    public void submitForApproval(Long id) {
        TeachingClass teachingClass = classRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        teachingClass.setStatus("PENDING_APPROVAL");
        classRepo.save(teachingClass);
    }

    /**
     * Xóa lớp học
     */
    @Transactional
    public void deleteClass(Long id) {
        TeachingClass teachingClass = classRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        classRepo.delete(teachingClass);
    }

    /**
     * Convert TeachingClass to ClassResponse
     */
    private ClassResponse convertToResponse(TeachingClass teachingClass) {
        ClassResponse response = new ClassResponse();
        response.setId(teachingClass.getId());
        response.setCode(teachingClass.getCode());
        response.setTitle(teachingClass.getTitle());
        response.setGradeLevel(teachingClass.getGradeLevel());
        response.setSubjectName(teachingClass.getSubject() != null ? teachingClass.getSubject().getName() : null);
        response.setTeacherUsername(teachingClass.getTeacher() != null ? teachingClass.getTeacher().getFullName() : null);
        response.setMode(teachingClass.getMode());
        response.setLocation(teachingClass.getLocation());
        response.setTuition(teachingClass.getTuition());
        response.setStatus(teachingClass.getStatus());
        response.setStartDate(teachingClass.getStartDate());
        response.setEndDate(teachingClass.getEndDate());

        // Convert schedules
        List<Map<String, Object>> scheduleList = scheduleRepo.findByTeachingClass(teachingClass)
                .stream()
                .map(schedule -> {
                    Map<String, Object> scheduleMap = new HashMap<>();
                    scheduleMap.put("id", schedule.getId());
                    scheduleMap.put("dayOfWeek", schedule.getDayOfWeek());
                    scheduleMap.put("startTime", schedule.getStartTime());
                    scheduleMap.put("endTime", schedule.getEndTime());
                    scheduleMap.put("note", schedule.getNote());
                    return scheduleMap;
                })
                .collect(Collectors.toList());
        
        response.setSchedules(scheduleList);
        return response;
    }
}