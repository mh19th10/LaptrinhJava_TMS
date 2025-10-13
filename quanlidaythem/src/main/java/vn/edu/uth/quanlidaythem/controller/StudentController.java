package vn.edu.uth.quanlidaythem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.dto.Request.UpdateStudentProfileRequest;
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
    public ResponseEntity<UserInfoResponse> profile(
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
}
