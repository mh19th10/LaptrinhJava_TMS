package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    // -------- SETTINGS --------
    // GET /api/admin/settings
    @GetMapping("/settings")
    public ResponseEntity<Map<String,Object>> getSettings() {
        Map<String,Object> s = new HashMap<>();
        s.put("systemName","TMS");
        s.put("allowExternalPayment", true);
        s.put("classMaxSize", 30);
        return ResponseEntity.ok(s);
    }

    // PUT /api/admin/settings/system
    @PutMapping("/settings/system")
    public ResponseEntity<Void> updateSystem(@RequestBody Map<String,Object> body) {
        // TODO: settingsService.updateSystem(body)
        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/settings/config
    @PutMapping("/settings/config")
    public ResponseEntity<Void> updateConfig(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/settings/fee-config
    @PutMapping("/settings/fee-config")
    public ResponseEntity<Void> updateFeeConfig(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/settings/class-config
    @PutMapping("/settings/class-config")
    public ResponseEntity<Void> updateClassConfig(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok().build();
    }

    // -------- MAINTENANCE / UTIL --------
    // POST /api/admin/backup/create
    @PostMapping("/backup/create")
    public ResponseEntity<Map<String,Object>> createBackup() {
        return ResponseEntity.ok(Map.of("backupId", UUID.randomUUID().toString()));
    }

    // POST /api/admin/backup/restore
    @PostMapping("/backup/restore")
    public ResponseEntity<Void> restoreBackup(@RequestBody Map<String,String> body) {
        // body.backupId
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/cache/clear
    @PostMapping("/cache/clear")
    public ResponseEntity<Void> clearCache() {
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/system/reset
    @PostMapping("/system/reset")
    public ResponseEntity<Void> resetSystem() {
        return ResponseEntity.ok().build();
    }

    // DELETE /api/admin/data/delete-all
    @DeleteMapping("/data/delete-all")
    public ResponseEntity<Void> deleteAllData() {
        return ResponseEntity.ok().build();
    }
}
