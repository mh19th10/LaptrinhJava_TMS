package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    // GET /api/admin/reports/summary?from=&to=
    @GetMapping("/summary")
    public ResponseEntity<Map<String,Object>> summary(
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to) {
        return ResponseEntity.ok(Map.of(
            "teachers",42,"classes",67,"students",1234,"revenue",450_000_000));
    }

    // GET /api/admin/reports/growth?period=monthly
    @GetMapping("/growth")
    public ResponseEntity<Map<String,Object>> growth(@RequestParam(defaultValue="monthly") String period) {
        return ResponseEntity.ok(Map.of("period",period,"series", List.of(12,19,15,25,22,30,28,35,32,40)));
    }

    // GET /api/admin/reports/subjects
    @GetMapping("/subjects")
    public ResponseEntity<Map<String,Object>> subjects() {
        return ResponseEntity.ok(Map.of("math",40,"physics",30,"chemistry",20,"literature",10));
    }

    // GET /api/admin/reports/revenue?year=2025
    @GetMapping("/revenue")
    public ResponseEntity<Map<String,Object>> revenue(@RequestParam Integer year) {
        return ResponseEntity.ok(Map.of("year",year,"months", List.of(30,32,28,35,40,38,42,45,47,50,0,0)));
    }

    // GET /api/admin/reports/top-teachers?limit=10
    @GetMapping("/top-teachers")
    public ResponseEntity<List<Map<String,Object>>> topTeachers(@RequestParam(defaultValue="10") int limit) {
        return ResponseEntity.ok(List.of(Map.of("id",1,"name","GV A","score",95)));
    }

    // GET /api/admin/reports/compliance
    @GetMapping("/compliance")
    public ResponseEntity<Map<String,Object>> compliance() {
        return ResponseEntity.ok(Map.of("tt29","OK","notes","Đáp ứng tiêu chí chính"));
    }

    // GET /api/admin/reports/export?format=excel&type=summary&...
    @GetMapping("/export")
    public ResponseEntity<Map<String,String>> export(@RequestParam String format, @RequestParam String type) {
        return ResponseEntity.ok(Map.of("message","Export queued","format",format,"type",type));
    }
}
