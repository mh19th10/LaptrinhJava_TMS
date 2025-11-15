package vn.edu.uth.quanlidaythem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.ScheduleEntity;
import vn.edu.uth.quanlidaythem.model.TeacherEntity;
import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.ScheduleRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherRepository;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<ClassEntity> getAllClasses() {
        return classRepository.findAll();
    }

    @Override
    public ClassEntity createClass(ClassEntity classEntity) {
        // Rule: Nếu là "in-school" thì không có học phí
        if ("in-school".equalsIgnoreCase(classEntity.getType())) {
            classEntity.setStatus("pending");
        }
        return classRepository.save(classEntity);
    }

    @Override
    public ClassEntity assignTeacher(Long classId, Long teacherId) {
        Optional<ClassEntity> classOpt = classRepository.findById(classId);
        Optional<TeacherEntity> teacherOpt = teacherRepository.findById(teacherId);

        if (classOpt.isPresent() && teacherOpt.isPresent()) {
            TeacherEntity teacher = teacherOpt.get();
            if (!"approved".equalsIgnoreCase(teacher.getStatus())) {
                throw new RuntimeException("Giáo viên chưa được phê duyệt!");
            }

            ClassEntity classEntity = classOpt.get();
            classEntity.setTeacher(teacher);
            return classRepository.save(classEntity);
        }
        throw new RuntimeException("Không tìm thấy lớp hoặc giáo viên");
    }

    @Override
    public ClassEntity getClassById(Long id) {
        return classRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với ID: " + id));
    }

    @Override
    public ScheduleEntity createSchedule(Long classId, ScheduleEntity schedule) {
        Optional<ClassEntity> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) throw new RuntimeException("Không tìm thấy lớp học!");

        // Rule: Giờ học hợp lệ (7h - 21h)
        if (schedule.getStartTime().isBefore(java.time.LocalTime.of(7,0)) ||
            schedule.getEndTime().isAfter(java.time.LocalTime.of(21,0))) {
            throw new RuntimeException("Thời gian học không hợp lệ (phải trong khung 7h-21h)");
        }

        schedule.setClassEntity(classOpt.get());
        return scheduleRepository.save(schedule);
    }
}
