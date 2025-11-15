package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.model.ScheduleEntity;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    // tiện ích: lấy list theo class id
    List<ScheduleEntity> findByClassEntityId(Long classId);
}
