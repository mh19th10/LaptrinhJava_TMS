package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/class-management") // ✅ ĐỔI ENDPOINT
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassController {

    // GET /api/admin/class-management?status=&type=&subject=
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String type,
            @RequestParam(required=false) String subject) {
        return ResponseEntity.ok(List.of(
            Map.of("id",101,"name","Toán cao cấp 101","status","ACTIVE"),
            Map.of("id",102,"name","Hóa cơ bản","status","PENDING")
        ));
    }

    // GET /api/admin/class-management/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("id",id,"name","Lớp #"+id,"status","PENDING"));
    }

    // POST /api/admin/class-management/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/class-management/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestBody(required=false) ReasonDTO body) {
        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/class-management/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> classData) {
        return ResponseEntity.ok().build();
    }

    // GET /api/admin/class-management/{id}/students
    @GetMapping("/{id}/students")
    public ResponseEntity<List<Map<String, Object>>> students(@PathVariable Long id) {
        return ResponseEntity.ok(List.of(
            Map.of("id",1,"name","HS A"), Map.of("id",2,"name","HS B")
        ));
    }

    // GET /api/admin/class-management/search?q=...
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String q) {
        return ResponseEntity.ok(List.of(Map.of("id",777,"name","Lớp tìm thấy: "+q)));
    }

    public static class ReasonDTO { public String reason; }
}