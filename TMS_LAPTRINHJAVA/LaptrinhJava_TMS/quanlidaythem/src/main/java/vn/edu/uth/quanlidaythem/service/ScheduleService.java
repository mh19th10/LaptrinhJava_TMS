package vn.edu.uth.quanlidaythem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.*;
import vn.edu.uth.quanlidaythem.dto.Request.ScheduleRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ScheduleResponse;
import vn.edu.uth.quanlidaythem.repository.*;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final TeachingClassRepository classRepo;

    public ScheduleService(ScheduleRepository scheduleRepo, TeachingClassRepository classRepo) {
        this.scheduleRepo = scheduleRepo;
        this.classRepo = classRepo;
    }

    /**
     * Add schedule to class. Validate no time overlap within same class.
     * Also optionally can check teacher's other classes to avoid teacher schedule collision.
     */
    @Transactional
    public ScheduleResponse addSchedule(ScheduleRequest req) {
        TeachingClass tc = classRepo.findById(req.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        // Validate times
        if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
            throw new IllegalArgumentException("StartTime must be before EndTime");
        }

        // Check overlap with existing schedules of the SAME class on same day
        List<Schedule> existing = scheduleRepo.findByTeachingClass(tc);
        for (Schedule s : existing) {
            if (s.getDayOfWeek().equals(req.getDayOfWeek())) {
                boolean overlap = !(req.getEndTime().isBefore(s.getStartTime()) || req.getStartTime().isAfter(s.getEndTime()));
                if (overlap) {
                    throw new IllegalArgumentException("Schedule overlaps with existing schedule for this class");
                }
            }
        }

        // Optional: check teacher's other classes to avoid teacher conflict
        if (tc.getTeacher() != null) {
            // fetch schedules of all classes of this teacher and check overlap on same day
            List<Schedule> teacherSchedules = scheduleRepo.findAll().stream()
                    .filter(sch -> sch.getTeachingClass().getTeacher() != null
                            && sch.getTeachingClass().getTeacher().getId().equals(tc.getTeacher().getId()))
                    .collect(Collectors.toList());
            for (Schedule ts : teacherSchedules) {
                if (ts.getDayOfWeek().equals(req.getDayOfWeek())) {
                    boolean overlap = !(req.getEndTime().isBefore(ts.getStartTime()) || req.getStartTime().isAfter(ts.getEndTime()));
                    if (overlap) {
                        throw new IllegalArgumentException("Schedule conflicts with teacher's other class");
                    }
                }
            }
        }

        Schedule s = new Schedule();
        s.setDayOfWeek(req.getDayOfWeek());
        s.setStartTime(req.getStartTime());
        s.setEndTime(req.getEndTime());
        s.setNote(req.getNote());
        s.setTeachingClass(tc);

        Schedule saved = scheduleRepo.save(s);
        return new ScheduleResponse(saved.getId(), saved.getDayOfWeek(), saved.getStartTime(), saved.getEndTime(), saved.getNote());
    }

    public List<ScheduleResponse> getSchedulesForClass(Long classId) {
        TeachingClass tc = classRepo.findById(classId).orElseThrow(() -> new IllegalArgumentException("Class not found"));
        return scheduleRepo.findByTeachingClass(tc).stream()
                .map(s -> new ScheduleResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(), s.getNote()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeSchedule(Long scheduleId) {
        Schedule s = scheduleRepo.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        scheduleRepo.delete(s);
    }
}
