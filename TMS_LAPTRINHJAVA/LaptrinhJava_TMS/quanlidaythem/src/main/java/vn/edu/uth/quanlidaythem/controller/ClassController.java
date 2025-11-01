package vn.edu.uth.quanlidaythem.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.uth.quanlidaythem.dto.Request.CreateClassRequest;
import vn.edu.uth.quanlidaythem.dto.Request.AssignTeacherRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ClassResponse;
import vn.edu.uth.quanlidaythem.service.ClassService;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    public ResponseEntity<ClassResponse> createClass(@RequestBody CreateClassRequest request) {
        var teachingClass = classService.createClass(request);
        return ResponseEntity.ok(ClassResponse.fromEntity(teachingClass));
    }

    @GetMapping
    public ResponseEntity<List<ClassResponse>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassResponse> getClassById(@PathVariable Long id) {
        var teachingClass = classService.getClassById(id);
        return ResponseEntity.ok(ClassResponse.fromEntity(teachingClass));
    }

    @PostMapping("/assign-teacher")
    public ResponseEntity<ClassResponse> assignTeacher(@RequestBody AssignTeacherRequest request) {
        var teachingClass = classService.assignTeacher(request);
        return ResponseEntity.ok(ClassResponse.fromEntity(teachingClass));
    }

    @PostMapping("/{id}/submit-approval")
    public ResponseEntity<Void> submitForApproval(@PathVariable Long id) {
        classService.submitForApproval(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
