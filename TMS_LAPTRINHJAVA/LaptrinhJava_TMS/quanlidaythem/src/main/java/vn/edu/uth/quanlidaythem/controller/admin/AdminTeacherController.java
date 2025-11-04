package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/teachers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeacherController {

    // GET /api/admin/teachers?status=&subject=
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("id",1,"name","Nguyễn Văn A","subject","Toán","status","PENDING"));
        items.add(Map.of("id",2,"name","Trần Thị B","subject","Vật lý","status","ACTIVE"));
        return ResponseEntity.ok(items);
    }

    // GET /api/admin/teachers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("id",id,"name","Giáo viên #" + id,"status","PENDING"));
    }

    // POST /api/admin/teachers/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        // TODO: teacherService.approve(id);
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/teachers/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestBody(required=false) ReasonDTO body) {
        // TODO: teacherService.reject(id, body != null ? body.reason : null);
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/teachers/{id}/suspend
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long id, @RequestBody(required=false) ReasonDTO body) {
        // TODO: teacherService.suspend(id, body != null ? body.reason : null);
        return ResponseEntity.ok().build();
    }

    // GET /api/admin/teachers/search?q=...
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String q) {
        return ResponseEntity.ok(List.of(Map.of("id",9,"name","Kết quả cho \""+q+"\"")));
    }

    public static class ReasonDTO { public String reason; }
}
