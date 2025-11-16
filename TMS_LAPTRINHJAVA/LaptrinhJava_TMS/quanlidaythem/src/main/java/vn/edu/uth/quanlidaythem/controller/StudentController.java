package vn.edu.uth.quanlidaythem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.Request.UpdateStudentProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ClassView;
import vn.edu.uth.quanlidaythem.dto.Response.StudentProfileResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.service.StudentService;
import vn.edu.uth.quanlidaythem.service.UserDetailsImpl;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** Lấy username từ principal; fallback sang Authentication.getName() */
    private String extractUsername(UserDetailsImpl user, Authentication auth) {
        if (user != null) return user.getUsername();
        if (auth != null) return auth.getName();
        return null;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> info(
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.getInfo(username));
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> profile(
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.getProfile(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> update(
            @RequestBody UpdateStudentProfileRequest req,
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        studentService.updateProfile(username, req);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách lớp học có thể đăng ký
     */
    @GetMapping("/classes/available")
    public ResponseEntity<List<ClassView>> getAvailableClasses(
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.getAvailableClasses(username));
    }

    /**
     * Lấy danh sách lớp học sinh đã đăng ký
     */
    @GetMapping("/classes/my")
    public ResponseEntity<List<ClassView>> getMyClasses(
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.getMyClasses(username));
    }

    /**
     * Đăng ký lớp học
     */
    @PostMapping("/classes/{classId}/register")
    public ResponseEntity<ClassView> registerClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.registerClass(username, classId));
    }

    /**
     * Hủy đăng ký lớp học
     */
    @DeleteMapping("/classes/{classId}/register")
    public ResponseEntity<Void> unregisterClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetailsImpl user,
            Authentication authentication
    ) {
        String username = extractUsername(user, authentication);
        if (username == null) return ResponseEntity.status(401).build();
        studentService.unregisterClass(username, classId);
        return ResponseEntity.ok().build();
    }
}
