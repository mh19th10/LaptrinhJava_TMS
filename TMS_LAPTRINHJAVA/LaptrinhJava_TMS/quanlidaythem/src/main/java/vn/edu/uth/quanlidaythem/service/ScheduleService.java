package vn.edu.uth.quanlidaythem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import vn.edu.uth.quanlidaythem.domain.*;
import vn.edu.uth.quanlidaythem.dto.Request.*;
import vn.edu.uth.quanlidaythem.repository.*;

@Service
public class ScheduleService {
    private final TeachingClassRepository classRepo;
    private final ClassScheduleSlotRepository slotRepo;
    private final CurriculumTopicRepository topicRepo;

    public ScheduleService(TeachingClassRepository c, ClassScheduleSlotRepository s, CurriculumTopicRepository t) {
        this.classRepo = c; this.slotRepo = s; this.topicRepo = t;
    }

    @Transactional
    public ClassScheduleSlot addSlot(AddScheduleSlotRequest req){
        TeachingClass clazz = classRepo.findById(req.classId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp"));
        ClassScheduleSlot slot = new ClassScheduleSlot();
        slot.setTeachingClass(clazz);
        slot.setDayOfWeek(DayOfWeek.valueOf(req.dayOfWeek));
        slot.setStartTime(LocalTime.parse(req.start));
        slot.setEndTime(LocalTime.parse(req.end));
        if (req.topicId != null) {
            CurriculumTopic topic = topicRepo.findById(req.topicId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topic"));
            slot.setTopic(topic);
        }
        return slotRepo.save(slot);
    }

    public List<ClassScheduleSlot> byClass(Long classId){
        return slotRepo.findByTeachingClass_Id(classId);
    }
}