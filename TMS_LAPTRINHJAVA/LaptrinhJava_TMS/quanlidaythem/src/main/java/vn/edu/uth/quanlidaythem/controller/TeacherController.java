package vn.edu.uth.quanlidaythem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.uth.quanlidaythem.domain.Teacher;
import vn.edu.uth.quanlidaythem.dto.Request.ScheduleRequest;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateTeacherProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ScheduleResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherClassItemResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherProfileResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.service.TeacherProfileService;
import vn.edu.uth.quanlidaythem.service.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherProfileService profileService;
    private final TeacherService teacherService;

    public TeacherController(TeacherProfileService profileService, TeacherService teacherService) {
        this.profileService = profileService;
        this.teacherService = teacherService;
    }

    // ================= Teacher: Profile / Info / Classes =================
    @GetMapping("/info")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserInfoResponse> info(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(teacherService.getInfo(username));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherProfileResponse getProfile() {
        return profileService.getProfile();
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherProfileResponse updateProfile(@RequestBody UpdateTeacherProfileRequest req) {
        return profileService.updateProfile(req);
    }

    @GetMapping("/classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherClassItemResponse>> classes(Authentication auth) {
        return ResponseEntity.ok(teacherService.getTeachingClasses(auth.getName()));
    }

    // ================= Admin: Quản lý giáo viên =================
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> register(@RequestBody Teacher teacher) {
        return ResponseEntity.ok(teacherService.registerTeacher(teacher));
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.approveTeacher(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @PostMapping("/{id}/subject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> addSubject(@PathVariable Long id, @RequestBody String subjectName) {
        return ResponseEntity.ok(teacherService.addSubject(id, subjectName));
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> addSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(teacherService.addSchedule(id, request));
    }
}
