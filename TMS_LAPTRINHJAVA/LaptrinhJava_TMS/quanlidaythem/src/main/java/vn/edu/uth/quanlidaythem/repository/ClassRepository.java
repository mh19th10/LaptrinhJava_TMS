package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;

import java.util.Optional;
import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<TeachingClass, Long> {

    // Tìm lớp theo tên
    Optional<TeachingClass> findByClassName(String className);

    // Lấy danh sách lớp theo giáo viên
    List<TeachingClass> findByTeacherId(Long teacherId);

    // Lấy danh sách lớp theo môn học
    List<TeachingClass> findBySubjectId(Long subjectId);
}
