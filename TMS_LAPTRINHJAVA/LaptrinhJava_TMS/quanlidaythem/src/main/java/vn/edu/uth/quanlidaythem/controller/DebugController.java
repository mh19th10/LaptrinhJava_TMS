package vn.edu.uth.quanlidaythem.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.TeacherRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("authenticated", auth != null && auth.isAuthenticated());
        response.put("principal", auth != null ? auth.getPrincipal() : null);
        response.put("authorities", auth != null ? auth.getAuthorities() : null);
        response.put("name", auth != null ? auth.getName() : null);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/database-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long userCount = userRepository.count();
            long classCount = classRepository.count();
            long teacherCount = teacherRepository.count();
            
            response.put("status", "connected");
            response.put("userCount", userCount);
            response.put("classCount", classCount);
            response.put("teacherCount", teacherCount);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        response.put("osVersion", System.getProperty("os.version"));
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}