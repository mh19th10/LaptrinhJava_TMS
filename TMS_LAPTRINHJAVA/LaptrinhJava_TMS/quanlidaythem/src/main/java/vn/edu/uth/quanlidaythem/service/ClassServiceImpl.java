package vn.edu.uth.quanlidaythem.service;

import java.time.Duration;
import java.util.List;

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
        if (classEntity.getType() == null) classEntity.setType("out-school");
        classEntity.setStatus("pending");
        return classRepository.save(classEntity);
    }

    @Override
    public ClassEntity assignTeacher(Long classId, Long teacherId) {
        ClassEntity classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));

        TeacherEntity teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên!"));

        // 1) Kiểm tra trạng thái teacher
        if (teacher.getStatus() == null || !"approved".equalsIgnoreCase(teacher.getStatus())) {
            throw new RuntimeException("Giáo viên chưa được phê duyệt!");
        }
        if (!teacher.isActive()) {
            throw new RuntimeException("Giáo viên hiện không hoạt động");
        }

        // 2) Kiểm tra môn dạy
        if (classEntity.getSubject() != null && !teacher.canTeachSubject(classEntity.getSubject())) {
            throw new RuntimeException("Giáo viên không được phép dạy môn này");
        }

        // 3) Kiểm tra xung đột lịch (so sánh schedules của lớp với schedules của giáo viên)
        List<ScheduleEntity> teacherSchedules = scheduleRepository.findByClassEntity_Teacher_Id(classEntity.getTeacher().getId());

        List<ScheduleEntity> classSchedules = classEntity.getSchedules();

        for (ScheduleEntity cs : classSchedules) {
            for (ScheduleEntity ts : teacherSchedules) {
                if (isSameDay(cs.getDayOfWeek(), ts.getDayOfWeek()) && isOverlap(cs.getStartTime(), cs.getEndTime(), ts.getStartTime(), ts.getEndTime())) {
                    throw new RuntimeException("Giáo viên bị trùng lịch dạy (trùng " + cs.getDayOfWeek() + " " + cs.getStartTime() + "-" + cs.getEndTime() + ")");
                }
            }
        }

        // 4) Kiểm tra tổng giờ/tuần (tùy chọn)
        int teacherMaxHours = teacher.getMaxWeeklyHours() == null ? 20 : teacher.getMaxWeeklyHours();
        long existingMinutes = teacherSchedules.stream()
            .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
            .sum();

        long classMinutes = classSchedules.stream()
            .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
            .sum();

        if ((existingMinutes + classMinutes) > teacherMaxHours * 60L) {
            throw new RuntimeException("Phân công vượt quá giới hạn giờ dạy của giáo viên trong tuần");
        }

        // all checks passed -> assign
        classEntity.setTeacher(teacher);
        ClassEntity saved = classRepository.save(classEntity);

        // Optionally set teacherId on schedules (if schedule keeps teacher reference)
        // for (ScheduleEntity s : classSchedules) {
        //     s.setTeacher(teacher);
        //     scheduleRepository.save(s);
        // }

        return saved;
    }

    @Override
    public ClassEntity getClassById(Long id) {
        return classRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với ID: " + id));
    }

    @Override
    public ScheduleEntity createSchedule(Long classId, ScheduleEntity schedule) {
        ClassEntity classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));

        // Rule: Giờ học hợp lệ (7h - 21h)
        if (schedule.getStartTime().isBefore(java.time.LocalTime.of(7,0)) ||
            schedule.getEndTime().isAfter(java.time.LocalTime.of(21,0)) ||
            schedule.getEndTime().isBefore(schedule.getStartTime())) {
            throw new RuntimeException("Thời gian học không hợp lệ (phải trong khung 7h-21h và end > start)");
        }

        // Optional: nếu lớp đã có teacher, cần kiểm tra trùng lịch với teacher
        if (classEntity.getTeacher() != null) {
            List<ScheduleEntity> teacherSchedules = scheduleRepository.findByClassEntity_Teacher_Id(classEntity.getTeacher().getId());
            for (ScheduleEntity ts : teacherSchedules) {
                if (isSameDay(ts.getDayOfWeek(), schedule.getDayOfWeek()) && isOverlap(ts.getStartTime(), ts.getEndTime(), schedule.getStartTime(), schedule.getEndTime())) {
                    throw new RuntimeException("Lịch mới trùng với lịch dạy hiện tại của giáo viên");
                }
            }
        }

        schedule.setClassEntity(classEntity);
        ScheduleEntity saved = scheduleRepository.save(schedule);

        // add to class and save
        classEntity.getSchedules().add(saved);
        classRepository.save(classEntity);

        return saved;
    }

    // ========== helpers ==========
    private boolean isSameDay(String d1, String d2) {
        if (d1 == null || d2 == null) return false;
        return d1.equalsIgnoreCase(d2);
    }

    private boolean isOverlap(java.time.LocalTime s1, java.time.LocalTime e1, java.time.LocalTime s2, java.time.LocalTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false;
        return !e1.isBefore(s2) && !e2.isBefore(s1); // overlap if ranges intersect
    }
}
