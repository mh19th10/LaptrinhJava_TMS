package vn.edu.uth.quanlidaythem.controller.teacher;

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

import vn.edu.uth.quanlidaythem.dto.Response.TeacherPermissionView;
import vn.edu.uth.quanlidaythem.service.TeacherPermissionService;

@RestController
@RequestMapping("/api/teacher/permissions")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPermissionController {

    private final TeacherPermissionService service;

    public TeacherPermissionController(TeacherPermissionService s) { this.service = s; }

    @GetMapping
    public ResponseEntity<List<TeacherPermissionView>> myPermissions(Authentication auth) {
        return ResponseEntity.ok(service.myPermissions(auth.getName()));
    }

    public static class ReqDTO { public Long subjectId; }

    @PostMapping
    public ResponseEntity<TeacherPermissionView> request(Authentication auth, @RequestBody ReqDTO body) {
        return ResponseEntity.ok(service.teacherRequest(auth.getName(), body.subjectId));
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> cancel(Authentication auth, @PathVariable Long permissionId) {
        service.teacherCancel(auth.getName(), permissionId);
        return ResponseEntity.noContent().build();
    }
}
