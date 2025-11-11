// src/main/java/vn/edu/uth/quanlidaythem/controller/teacher/TeacherRegistrationController.java
package vn.edu.uth.quanlidaythem.controller.teacher;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.uth.quanlidaythem.domain.TeacherRegistration;
import vn.edu.uth.quanlidaythem.service.TeacherRegistrationService;

@RestController
@RequestMapping("/api/teacher/registrations")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherRegistrationController {

    private final TeacherRegistrationService service;

    public TeacherRegistrationController(TeacherRegistrationService s) { this.service = s; }

    @GetMapping
    public ResponseEntity<List<TeacherRegistration>> myRegistrations(Authentication auth) {
        return ResponseEntity.ok(service.myRegistrations(auth.getName()));
    }

    // ==== Đăng ký vào lớp có sẵn ====
    public static class RegisterIntoClassReq { public Long classId; }
    @PostMapping
    public ResponseEntity<TeacherRegistration> registerIntoClass(Authentication auth,
                                                                 @Valid @RequestBody RegisterIntoClassReq req) {
        return ResponseEntity.ok(service.registerIntoClass(auth.getName(), req.classId));
    }

    // ==== Đề nghị mở lớp mới (CUSTOM) – KHÔNG gửi classId ====
    public static class CustomReq {
        public String  className;
        public Long    subjectId;
        public Integer capacity;
        public String  mode;
        public LocalDate startDate;
        public String  location;
        public String  schedule;
        public String  note;
    }

    @PostMapping("/custom")
    public ResponseEntity<TeacherRegistration> registerCustom(Authentication auth,
                                                              @Valid @RequestBody CustomReq req) {
        return ResponseEntity.ok(
            service.registerCustom(
                auth.getName(),
                req.className,
                req.subjectId,
                req.capacity,
                req.mode,
                req.startDate,
                req.location,
                req.schedule,
                req.note
            )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(Authentication auth, @PathVariable Long id) {
        service.cancelRegistration(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
