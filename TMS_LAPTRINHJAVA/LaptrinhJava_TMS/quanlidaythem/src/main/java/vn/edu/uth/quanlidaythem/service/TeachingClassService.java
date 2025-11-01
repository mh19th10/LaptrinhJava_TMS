package vn.edu.uth.quanlidaythem.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.*;
import vn.edu.uth.quanlidaythem.dto.Request.*;
import vn.edu.uth.quanlidaythem.dto.Response.*;
import vn.edu.uth.quanlidaythem.repository.*;

@Service
public class TeachingClassService {

    private final TeachingClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final ScheduleRepository scheduleRepo;

    public TeachingClassService(TeachingClassRepository classRepo,
                              SubjectRepository subjectRepo,
                              TeacherRepository teacherRepo,
                              ScheduleRepository scheduleRepo) {
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.scheduleRepo = scheduleRepo;
    }

    /**
     * Tạo lớp học
     */
    @Transactional
    public ClassResponse createClass(String username, CreateTeachingClassRequest req) {
        Subject subject = subjectRepo.findByCode(req.getSubjectCode())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + req.getSubjectCode()));

        // Tìm teacher nếu có teacherId
        Teacher teacher = null;
        if (req.getTeacherId() != null) {
            teacher = teacherRepo.findById(req.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + req.getTeacherId()));
        }

        // Validation
        if (req.getGradeLevel() < subject.getGradeLevel()) {
            throw new IllegalArgumentException("Không được dạy trước chương trình");
        }

        if ("OUT_OF_SCHOOL".equalsIgnoreCase(req.getMode()) && (req.getTuition() == null || req.getTuition() <= 0)) {
            throw new IllegalArgumentException("Lớp ngoài trường phải có học phí hợp lệ");
        }

        TeachingClass tc = new TeachingClass();
        tc.setCode(req.getCode());
        tc.setTitle(req.getTitle());
        tc.setGradeLevel(req.getGradeLevel());
        tc.setSubject(subject);
        tc.setTeacher(teacher);
        tc.setMode(req.getMode());
        tc.setLocation(req.getLocation());
        tc.setTuition(req.getTuition());
        tc.setStatus("DRAFT");
        tc.setStartDate(req.getStartDate());
        tc.setEndDate(req.getEndDate());

        classRepo.save(tc);
        return toResponse(tc);
    }

    /**
     * Cập nhật lớp học
     */
    @Transactional
    public ClassResponse updateClass(Long id, UpdateTeachingClassRequest req) {
        TeachingClass tc = classRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        if (req.getTitle() != null) tc.setTitle(req.getTitle());
        if (req.getGradeLevel() != null) tc.setGradeLevel(req.getGradeLevel());
        if (req.getMode() != null) tc.setMode(req.getMode());
        if (req.getLocation() != null) tc.setLocation(req.getLocation());
        if (req.getTuition() != null) tc.setTuition(req.getTuition());
        if (req.getStartDate() != null) tc.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) tc.setEndDate(req.getEndDate());
        
        // Cập nhật subject nếu có
        if (req.getSubjectId() != null) {
            Subject subject = subjectRepo.findById(req.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            tc.setSubject(subject);
        }
        
        // Cập nhật teacher nếu có
        if (req.getTeacherId() != null) {
            Teacher teacher = teacherRepo.findById(req.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            tc.setTeacher(teacher);
        }

        // Xử lý isActive (nếu có)
        if (req.getIsActive() != null) {
            tc.setStatus(req.getIsActive() ? "APPROVED" : "REJECTED");
        }

        classRepo.save(tc);
        return toResponse(tc);
    }

    /**
     * Lấy lớp theo teacher
     */
    public List<ClassResponse> getClassesByTeacher(Long teacherId) {
        Teacher teacher = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        return classRepo.findByTeacher(teacher).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate class
     */
    @Transactional
    public void deactivateClass(Long id) {
        TeachingClass tc = classRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        tc.setStatus("FINISHED");
        classRepo.save(tc);
    }

    // Các method khác
    public List<ClassResponse> getAllClasses() {
        return classRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ClassResponse getClassById(Long id) {
        TeachingClass tc = classRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Class not found"));
        return toResponse(tc);
    }

    @Transactional
    public void submitForApproval(Long classId) {
        TeachingClass tc = classRepo.findById(classId).orElseThrow(() -> new IllegalArgumentException("Class not found"));
        tc.setStatus("PENDING_APPROVAL");
        classRepo.save(tc);
    }

    @Transactional
    public TeachingClass approveClass(Long classId, boolean approve) {
        TeachingClass tc = classRepo.findById(classId).orElseThrow(() -> new IllegalArgumentException("Class not found"));
        tc.setStatus(approve ? "APPROVED" : "REJECTED");
        return classRepo.save(tc);
    }

    @Transactional
    public void deleteClass(Long id) {
        TeachingClass tc = classRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Class not found"));
        classRepo.delete(tc);
    }

    /**
     * Convert TeachingClass to ClassResponse - FIXED
     */
    private ClassResponse toResponse(TeachingClass tc) {
        ClassResponse response = new ClassResponse();
        response.setId(tc.getId());
        response.setCode(tc.getCode());
        response.setTitle(tc.getTitle());
        response.setGradeLevel(tc.getGradeLevel());
        response.setSubjectName(tc.getSubject() != null ? tc.getSubject().getName() : null);
        response.setTeacherUsername(tc.getTeacher() != null ? tc.getTeacher().getFullName() : null);
        response.setMode(tc.getMode());
        response.setLocation(tc.getLocation());
        response.setTuition(tc.getTuition());
        response.setStatus(tc.getStatus());
        response.setStartDate(tc.getStartDate());
        response.setEndDate(tc.getEndDate());

        // Convert schedules
        List<Map<String, Object>> scheduleList = scheduleRepo.findByTeachingClass(tc).stream().map(s -> {
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("id", s.getId());
            scheduleMap.put("dayOfWeek", s.getDayOfWeek());
            scheduleMap.put("startTime", s.getStartTime());
            scheduleMap.put("endTime", s.getEndTime());
            scheduleMap.put("note", s.getNote());
            return scheduleMap;
        }).collect(Collectors.toList());
        
        response.setSchedules(scheduleList);
        return response;
    }
}