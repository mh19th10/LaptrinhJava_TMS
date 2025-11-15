package vn.edu.uth.quanlidaythem.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.ScheduleEntity;
import vn.edu.uth.quanlidaythem.service.ClassService;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class ClassController {

    @Autowired
    private ClassService classService;

    @GetMapping
    public List<ClassEntity> getAllClasses() {
        return classService.getAllClasses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Long id) {
        try {
            ClassEntity c = classService.getClassById(id);
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // Lấy danh sách schedules của 1 lớp (trả ResponseEntity để frontend nhận body dễ dàng)
    @GetMapping("/{id}/schedules")
    public ResponseEntity<?> getSchedulesByClassId(@PathVariable Long id) {
        try {
            ClassEntity c = classService.getClassById(id);
            List<ScheduleEntity> schedules = c.getSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createClass(@RequestBody ClassEntity classEntity) {
        try {
            ClassEntity createdClass = classService.createClass(classEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo lớp học thành công");
            response.put("data", createdClass);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{classId}/assign-teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> assignTeacher(@PathVariable Long classId, @PathVariable Long teacherId) {
        try {
            ClassEntity updatedClass = classService.assignTeacher(classId, teacherId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Phân công giáo viên thành công");
            response.put("data", updatedClass);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Create schedule with error handling
    @PostMapping("/{classId}/schedule")
    public ResponseEntity<?> createSchedule(@PathVariable Long classId, @RequestBody ScheduleEntity schedule) {
        try {
            ScheduleEntity created = classService.createSchedule(classId, schedule);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
