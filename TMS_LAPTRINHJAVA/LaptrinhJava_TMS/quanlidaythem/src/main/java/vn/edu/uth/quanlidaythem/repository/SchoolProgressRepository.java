package vn.edu.uth.quanlidaythem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.SchoolProgress;

public interface SchoolProgressRepository extends JpaRepository<SchoolProgress, Long> {
    Optional<SchoolProgress> findBySubject_Id(Long subjectId);
}