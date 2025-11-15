package vn.edu.uth.quanlidaythem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.Request.UpdateTeacherProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherClassItemResponse;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherProfileResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.model.TeacherEntity;
import vn.edu.uth.quanlidaythem.repository.TeacherRepository;
import vn.edu.uth.quanlidaythem.service.TeacherProfileService;
import vn.edu.uth.quanlidaythem.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherProfileService profileService;
    private final TeacherService teacherService;

    public TeacherController(TeacherProfileService profileService, TeacherService teacherService) {
        this.profileService = profileService;
        this.teacherService = teacherService;
    }

    @Autowired
    private TeacherRepository teacherRepository;

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

    @GetMapping
    public ResponseEntity<List<TeacherEntity>> getAllTeachers(@RequestParam(required = false) String status) {
        List<TeacherEntity> teachers;
        if (status != null) {
            teachers = teacherRepository.findByStatus(status);
        } else {
            teachers = teacherRepository.findAll();
        }
        return ResponseEntity.ok(teachers);
    }

    /**
     * GET /api/teachers/approved - Lấy giáo viên đã duyệt
     */
    @GetMapping("/approved")
    public ResponseEntity<List<TeacherEntity>> getApprovedTeachers() {
        List<TeacherEntity> teachers = teacherRepository.findByStatus("approved");
        return ResponseEntity.ok(teachers);
    }
}
