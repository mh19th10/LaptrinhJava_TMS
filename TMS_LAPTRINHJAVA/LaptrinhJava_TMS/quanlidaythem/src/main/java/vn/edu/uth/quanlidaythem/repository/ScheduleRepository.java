package vn.edu.uth.quanlidaythem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.uth.quanlidaythem.domain.Schedule;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTeachingClass(TeachingClass teachingClass);
}
