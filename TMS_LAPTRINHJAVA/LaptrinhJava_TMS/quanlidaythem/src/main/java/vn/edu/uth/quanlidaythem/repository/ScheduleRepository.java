package vn.edu.uth.quanlidaythem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.model.ScheduleEntity;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    // Lấy tất cả schedule đã gắn với giáo viên (nếu bạn lưu teacher trong ScheduleEntity)
    List<ScheduleEntity> findByClassEntity_Teacher_Id(Long teacherId);

    // Nếu schedule lưu reference classEntity, bạn có thể cần:
    List<ScheduleEntity> findByClassEntityId(Long classId);
}
