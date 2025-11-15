package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.model.TeacherEntity;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    List<TeacherEntity> findByStatus(String status);
    
    long countByStatus(String status);


}