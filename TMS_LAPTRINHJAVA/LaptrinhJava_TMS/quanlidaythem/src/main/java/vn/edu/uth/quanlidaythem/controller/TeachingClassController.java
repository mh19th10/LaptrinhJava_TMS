package vn.edu.uth.quanlidaythem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vn.edu.uth.quanlidaythem.dto.Request.*;
import vn.edu.uth.quanlidaythem.dto.Response.*;
import vn.edu.uth.quanlidaythem.service.TeachingClassService;

@RestController
@RequestMapping("/api/classes")
public class TeachingClassController {

    private final TeachingClassService classService;

    public TeachingClassController(TeachingClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> createClass(
            @RequestHeader("username") String username, // FIX: thêm username
            @RequestBody CreateTeachingClassRequest req) {
        return ResponseEntity.ok(classService.createClass(username, req)); // FIXED
    }

    @GetMapping("/teacher/{teacherId}") // FIX: đổi từ username sang teacherId
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ClassResponse>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(classService.getClassesByTeacher(teacherId)); // FIXED
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable Long id, // FIX: thêm @PathVariable
            @RequestBody UpdateTeachingClassRequest req) {
        return ResponseEntity.ok(classService.updateClass(id, req)); // FIXED
    }

    @PutMapping("/{id}/deactivate") // THÊM: endpoint deactivate
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateClass(@PathVariable Long id) {
        classService.deactivateClass(id); // FIXED
        return ResponseEntity.ok("Class deactivated");
    }

    // Các endpoint khác giữ nguyên...
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<ClassResponse>> getAll() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<String> submitForApproval(@PathVariable Long id) {
        classService.submitForApproval(id);
        return ResponseEntity.ok("Submitted for approval");
    }
}