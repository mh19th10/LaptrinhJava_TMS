// src/main/java/vn/edu/uth/quanlidaythem/repository/TeacherRegistrationRepository.java
package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.TeacherRegistration;

public interface TeacherRegistrationRepository extends JpaRepository<TeacherRegistration, Long> {
    List<TeacherRegistration> findByTeacherIdOrderByIdDesc(Long teacherId);
}
