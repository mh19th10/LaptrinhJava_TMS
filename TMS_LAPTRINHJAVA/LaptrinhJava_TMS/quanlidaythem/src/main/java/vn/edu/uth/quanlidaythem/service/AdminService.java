package vn.edu.uth.quanlidaythem.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.StudentEntity;
import vn.edu.uth.quanlidaythem.model.TeacherEntity;

public interface AdminService {

    List<TeacherEntity> getAllTeachers();
    TeacherEntity updateTeacherStatus(Long teacherId, String status);

    
    // QUẢN LÝ LỚP HỌC
    Page<ClassEntity> getAllClasses(Pageable pageable, String status, String type);
    ClassEntity getClassById(Long classId);
    ClassEntity approveClass(Long classId);
    ClassEntity rejectClass(Long classId, String reason);
    void deleteClass(Long classId);
    
    // QUẢN LÝ HỌC SINH
    List<StudentEntity> getStudentsByClassId(Long classId);
    void addStudentToClass(Long classId, Long studentId);
    void removeStudentFromClass(Long classId, Long studentId);
    
    // THỐNG KÊ
    Map<String, Object> getOverviewStats();
    Map<String, Long> getClassesByStatus();
    Map<String, Long> getClassesByType();
    List<Map<String, Object>> getRecentActivities(int limit);
}