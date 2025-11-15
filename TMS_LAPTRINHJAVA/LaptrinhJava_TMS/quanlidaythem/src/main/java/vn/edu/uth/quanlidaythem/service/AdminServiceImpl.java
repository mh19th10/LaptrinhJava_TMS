package vn.edu.uth.quanlidaythem.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.StudentEntity;
import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.StudentRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherSubjectPermissionRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private TeacherSubjectPermissionRepository permissionRepo;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    

    @Override
    public Page<ClassEntity> getAllClasses(Pageable pageable, String status, String type) {
        if (status != null && !status.isEmpty() && type != null && !type.isEmpty()) {
            return classRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null && !status.isEmpty()) {
            return classRepository.findByStatus(status, pageable);
        } else if (type != null && !type.isEmpty()) {
            return classRepository.findByType(type, pageable);
        }
        return classRepository.findAll(pageable);
    }

    @Override
    public ClassEntity getClassById(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với ID: " + classId));
    }

    @Override
    public ClassEntity approveClass(Long classId) {
        ClassEntity classEntity = getClassById(classId);
        
        if (classEntity.getTeacher() == null) {
            throw new RuntimeException("Lớp học chưa có giáo viên, không thể duyệt!");
        }
        
        if (!"approved".equalsIgnoreCase(classEntity.getTeacher().getStatus())) {
            throw new RuntimeException("Giáo viên chưa được phê duyệt!");
        }
        
        if (classEntity.getSchedules() == null || classEntity.getSchedules().isEmpty()) {
            throw new RuntimeException("Lớp học chưa có lịch học, không thể duyệt!");
        }
        
        classEntity.setStatus("approved");
        return classRepository.save(classEntity);
    }

    @Override
    public ClassEntity rejectClass(Long classId, String reason) {
        ClassEntity classEntity = getClassById(classId);

        classEntity.setStatus("rejected");

        // Lưu lý do (nếu null thì để "Không có lý do")
        classEntity.setRejectReason(
            (reason == null || reason.isBlank()) ? "Không có lý do" : reason
        );

        return classRepository.save(classEntity);
    }

    @Override
    public void deleteClass(Long classId) {
        ClassEntity classEntity = getClassById(classId);
        
        if (classEntity.getStudents() != null && !classEntity.getStudents().isEmpty()) {
            throw new RuntimeException("Không thể xóa lớp đang có học sinh! Vui lòng xóa học sinh trước.");
        }
        
        classRepository.deleteById(classId);
    }

    @Override
    public List<StudentEntity> getStudentsByClassId(Long classId) {
        ClassEntity classEntity = getClassById(classId);
        return classEntity.getStudents();
    }

    @Override
    public void addStudentToClass(Long classId, Long studentId) {
        ClassEntity classEntity = getClassById(classId);
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh với ID: " + studentId));
        
        if (classEntity.getStudents().contains(student)) {
            throw new RuntimeException("Học sinh đã có trong lớp này!");
        }
        
        // Validation Thông tư 29
        if ("in-school".equalsIgnoreCase(classEntity.getType())) {
            String studentType = student.getStudentType();
            if (!"failed".equals(studentType) && 
                !"gifted".equals(studentType) && 
                !"final-year".equals(studentType)) {
                throw new RuntimeException(
                    "Lớp trong trường chỉ nhận học sinh: thi rớt, năng khiếu, hoặc năm cuối!"
                );
            }
        }
        
        classEntity.getStudents().add(student);
        classRepository.save(classEntity);
    }

    @Override
    public void removeStudentFromClass(Long classId, Long studentId) {
        ClassEntity classEntity = getClassById(classId);
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh với ID: " + studentId));
        
        classEntity.getStudents().remove(student);
        classRepository.save(classEntity);
    }

    @Override
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        // Tổng lớp, tổng học sinh
        stats.put("totalClasses", classRepository.count());
        stats.put("totalStudents", studentRepository.count());

        // Tổng số giáo viên từ bảng users
        stats.put("totalTeachers", userRepository.countByRoleIgnoreCase("TEACHER"));

        // Số giáo viên chờ duyệt từ bảng teacher_subject_permissions
        stats.put("pendingTeachers", 
            userRepository.countByRoleIgnoreCaseAndStatusIgnoreCase("TEACHER", "PENDING")
        );

        // Số lớp
        stats.put("activeClasses", classRepository.countByStatus("approved"));
        stats.put("pendingClasses", classRepository.countByStatus("pending"));

        stats.put("timestamp", LocalDateTime.now().toString());
        return stats;
        
    }

    @Override
    public Map<String, Long> getClassesByStatus() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("pending", classRepository.countByStatus("pending"));
        stats.put("approved", classRepository.countByStatus("approved"));
        stats.put("rejected", classRepository.countByStatus("rejected"));
        stats.put("active", classRepository.countByStatus("active"));
        
        return stats;
    }

    @Override
    public Map<String, Long> getClassesByType() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("in-school", classRepository.countByType("in-school"));
        stats.put("out-school", classRepository.countByType("out-school"));
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities(int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        List<ClassEntity> recentClasses = classRepository.findTop10ByOrderByIdDesc();
        
        for (ClassEntity c : recentClasses) {
            if (activities.size() >= limit) break;
            
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", c.getId());
            activity.put("type", "CLASS_CREATED");
            activity.put("className", c.getClassName());
            activity.put("subject", c.getSubject());
            activity.put("status", c.getStatus());
            activity.put("teacher", c.getTeacher() != null ? c.getTeacher().getFullName() : "Chưa phân công");
            
            activities.add(activity);
        }
        
        return activities;
    }
}