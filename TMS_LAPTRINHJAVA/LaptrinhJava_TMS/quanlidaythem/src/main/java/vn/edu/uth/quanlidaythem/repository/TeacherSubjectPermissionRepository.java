package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;

public interface TeacherSubjectPermissionRepository 
        extends JpaRepository<TeacherSubjectPermission, Long> {

    List<TeacherSubjectPermission> findByTeacher_Id(Long teacherId);

    boolean existsByTeacher_IdAndSubject_IdAndActiveTrue(Long teacherId, Long subjectId);

    @Modifying
    @Transactional
    @Query("UPDATE TeacherSubjectPermission p SET p.active = :active WHERE p.teacher.id = :teacherId")
    int updateActiveByTeacherId(Long teacherId, boolean active);
}
