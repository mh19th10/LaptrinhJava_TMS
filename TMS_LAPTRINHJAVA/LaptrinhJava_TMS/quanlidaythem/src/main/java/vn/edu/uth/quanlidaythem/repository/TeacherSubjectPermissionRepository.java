package vn.edu.uth.quanlidaythem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.TeacherSubjectPermission;

public interface TeacherSubjectPermissionRepository extends JpaRepository<TeacherSubjectPermission, Long> {
    List<TeacherSubjectPermission> findByTeacherId(Long teacherId);
    List<TeacherSubjectPermission> findByTeacherIdAndActiveTrue(Long teacherId);
    boolean existsByTeacherIdAndActiveTrue(Long teacherId);
    boolean existsByTeacherIdAndSubject_IdAndActiveTrue(Long teacherId, Long subjectId);
    Optional<TeacherSubjectPermission> findByTeacherIdAndSubject_IdAndActiveTrue(Long teacherId, Long subjectId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TeacherSubjectPermission p set p.active = :active where p.teacherId = :teacherId")
    int updateActiveByTeacherId(@Param("teacherId") Long teacherId, @Param("active") boolean active);
}
