package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.ClassScheduleSlot;

public interface ClassScheduleSlotRepository extends JpaRepository<ClassScheduleSlot, Long> {
    List<ClassScheduleSlot> findByTeachingClass_Id(Long classId);
}