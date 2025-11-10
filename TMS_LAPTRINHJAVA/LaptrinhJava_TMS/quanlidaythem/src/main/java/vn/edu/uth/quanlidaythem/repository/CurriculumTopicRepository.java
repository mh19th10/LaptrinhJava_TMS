package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uth.quanlidaythem.domain.CurriculumTopic;
import java.util.*;

public interface CurriculumTopicRepository extends JpaRepository<CurriculumTopic, Long> {
    List<CurriculumTopic> findBySubject_IdOrderByWeekIndexAsc(Long subjectId);
    Optional<CurriculumTopic> findBySubject_IdAndWeekIndex(Long subjectId, int weekIndex);
}