package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uth.quanlidaythem.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByCode(String code);
}