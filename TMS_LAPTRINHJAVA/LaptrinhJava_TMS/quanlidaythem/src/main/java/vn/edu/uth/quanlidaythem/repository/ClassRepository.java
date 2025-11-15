package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.model.ClassEntity;

/**
 * Class Repository - CẬP NHẬT ĐẦY ĐỦ
 * Thêm các query methods để hỗ trợ Admin Service
 */
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    
    // ============================================
    // COUNT METHODS - Dùng cho thống kê
    // ============================================
    
    /**
     * Đếm số lớp theo trạng thái
     */
    long countByStatus(String status);
    
    /**
     * Đếm số lớp theo loại (in-school, out-school)
     */
    long countByType(String type);
    
    // ============================================
    // FIND METHODS - Tìm kiếm với phân trang
    // ============================================
    
    /**
     * Tìm tất cả lớp theo trạng thái (có phân trang)
     */
    Page<ClassEntity> findByStatus(String status, Pageable pageable);
    
    /**
     * Tìm tất cả lớp theo loại (có phân trang)
     */
    Page<ClassEntity> findByType(String type, Pageable pageable);
    
    /**
     * Tìm lớp theo cả status và type (có phân trang)
     */
    Page<ClassEntity> findByStatusAndType(String status, String type, Pageable pageable);
    
    /**
     * Tìm các lớp của 1 giáo viên
     */
    List<ClassEntity> findByTeacher_Id(Long teacherId);
    
    /**
     * Tìm các lớp theo môn học
     */
    List<ClassEntity> findBySubject(String subject);
    
    // ============================================
    // CUSTOM QUERIES
    // ============================================
    
    /**
     * Lấy 10 lớp mới nhất (theo ID giảm dần)
     */
    List<ClassEntity> findTop10ByOrderByIdDesc();
    
    /**
     * Tìm kiếm lớp theo tên (like)
     */
    //@Query("SELECT c FROM ClassEntity c WHERE c.className LIKE %:keyword%")
    //List<ClassEntity> searchByClassName(@Param("keyword") String keyword);
    
    /**
     * Lấy các lớp chưa có giáo viên
     */
    //@Query("SELECT c FROM ClassEntity c WHERE c.teacher IS NULL")
    //List<ClassEntity> findClassesWithoutTeacher();
    
    /**
     * Lấy các lớp đã có giáo viên nhưng chưa có lịch
     */
    // @Query("SELECT c FROM ClassEntity c WHERE c.teacher IS NOT NULL AND SIZE(c.schedules) = 0")
    //List<ClassEntity> findClassesWithoutSchedule();
    
    /**
     * Đếm số lớp của 1 giáo viên
     */
    //@Query("SELECT COUNT(c) FROM ClassEntity c WHERE c.teacher.id = :teacherId")
    //long countByTeacherId(@Param("teacherId") Long teacherId);
}