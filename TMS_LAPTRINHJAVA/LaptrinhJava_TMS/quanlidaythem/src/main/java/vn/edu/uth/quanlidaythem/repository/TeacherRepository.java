package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uth.quanlidaythem.domain.Teacher;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);
}
