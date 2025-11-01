package vn.edu.uth.quanlidaythem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vn.edu.uth.quanlidaythem.dto.Request.ScheduleRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ScheduleResponse;
import vn.edu.uth.quanlidaythem.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> addSchedule(@RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.addSchedule(req));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('PARENT') or hasRole('STUDENT')")
    public ResponseEntity<List<ScheduleResponse>> getForClass(@PathVariable Long classId) {
        return ResponseEntity.ok(scheduleService.getSchedulesForClass(classId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<String> remove(@PathVariable Long id) {
        scheduleService.removeSchedule(id);
        return ResponseEntity.ok("Schedule removed");
    }
}
