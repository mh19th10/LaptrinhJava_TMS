package vn.edu.uth.quanlidaythem.controller.admin;

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
@RequestMapping("/api/admin/fees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeeController {

    // GET /api/admin/fees?status=&month=
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String month) {
        return ResponseEntity.ok(List.of(
            Map.of("id",501,"student","HS A","amount",500_000,"status","UNPAID"),
            Map.of("id",502,"student","HS B","amount",700_000,"status","PAID")
        ));
    }

    // GET /api/admin/fees/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("id",id,"amount",500_000,"status","UNPAID"));
    }

    // POST /api/admin/fees/{id}/pay
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> markAsPaid(@PathVariable Long id, @RequestBody PayRequest body) {
        // TODO: feeService.pay(id, body.amount, body.paymentMethod)
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/fees/{id}/remind
    @PostMapping("/{id}/remind")
    public ResponseEntity<Void> remind(@PathVariable Long id) {
        // TODO: feeService.remind(id)
        return ResponseEntity.ok().build();
    }

    // GET /api/admin/fees/statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(Map.of("month","10/2025","total",450_000_000,"paidRate",0.82));
    }

    // GET /api/admin/fees/export?format=excel|pdf&...
    @GetMapping("/export")
    public ResponseEntity<Map<String,String>> export(@RequestParam String format) {
        // Trả về link giả lập (FE đang tự tạo file từ blob, bạn có thể trả bytes sau)
        return ResponseEntity.ok(Map.of("message","Export queued","format",format));
    }

    public static class PayRequest {
        public Long amount;
        public String paymentMethod;
    }
}
