// src/main/java/vn/edu/uth/quanlidaythem/repository/TeacherRegistrationRepository.java
package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.TeacherRegistration;
import vn.edu.uth.quanlidaythem.domain.TeacherRegistration.Status;

public interface TeacherRegistrationRepository extends JpaRepository<TeacherRegistration, Long> {
    List<TeacherRegistration> findByTeacherIdOrderByIdDesc(Long teacherId);
    
    // Tìm các đăng ký pending và chưa gắn lớp (custom registration)
    List<TeacherRegistration> findByStatusAndTeachingClassIsNullOrderByIdDesc(Status status);
    
    // Tìm tất cả đăng ký theo status
    List<TeacherRegistration> findByStatusOrderByIdDesc(Status status);
}
