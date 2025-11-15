package vn.edu.uth.quanlidaythem.service;

import java.util.List;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.ScheduleEntity;

public interface ClassService {
    List<ClassEntity> getAllClasses();
    ClassEntity createClass(ClassEntity classEntity);
    ClassEntity getClassById(Long id);
    ClassEntity assignTeacher(Long classId, Long teacherId);
    ScheduleEntity createSchedule(Long classId, ScheduleEntity schedule);
    
}
