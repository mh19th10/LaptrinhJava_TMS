package vn.edu.uth.quanlidaythem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;
import vn.edu.uth.quanlidaythem.domain.Teacher; // SỬA

@Repository
public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {
    List<TeachingClass> findByTeacher(Teacher teacher); // SỬA: Teacher thay vì User
    List<TeachingClass> findByStatus(String status);
}