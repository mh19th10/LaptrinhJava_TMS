package vn.edu.uth.quanlidaythem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.Request.UpdateTeacherProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherProfileResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.service.TeacherProfileService;
import vn.edu.uth.quanlidaythem.service.TeacherService;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherProfileService profileService;
    private final TeacherService teacherService;

    public TeacherController(TeacherProfileService profileService, TeacherService teacherService) {
        this.profileService = profileService;
        this.teacherService = teacherService;
    }

    // FE (dashboard_teacher.html & assets/js/teacher.js) đang gọi endpoint này
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
}
