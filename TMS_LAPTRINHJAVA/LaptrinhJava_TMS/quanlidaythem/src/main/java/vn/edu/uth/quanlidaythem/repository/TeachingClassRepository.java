package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.TeachingClass;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {
    List<TeachingClass> findBySubject_Id(Long subjectId);
    List<TeachingClass> findByTeacherId(Long teacherId);
}