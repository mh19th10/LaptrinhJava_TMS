package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.model.StudentEntity;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    long countByClasses_Id(Long classId);
    @Override
    long count();

}

