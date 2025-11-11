package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.uth.quanlidaythem.domain.ClassScheduleSlot;
import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;
import vn.edu.uth.quanlidaythem.dto.Request.AddScheduleSlotRequest;
import vn.edu.uth.quanlidaythem.dto.Request.CreateClassRequest;
import vn.edu.uth.quanlidaythem.repository.TeachingClassRepository;
import vn.edu.uth.quanlidaythem.service.AdminApprovalService;
import vn.edu.uth.quanlidaythem.service.ScheduleService;

@RestController
@RequestMapping("/api/admin/teach")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeachingController {

    private final TeachingClassRepository classRepo;
    private final ScheduleService scheduleService;
    private final AdminApprovalService approvalService;

    public AdminTeachingController(
            TeachingClassRepository classRepo,
            ScheduleService scheduleService,
            AdminApprovalService approvalService
    ) {
        this.classRepo = classRepo;
        this.scheduleService = scheduleService;
        this.approvalService = approvalService;
    }

    // ===================== LỚP =====================
    @PostMapping("/classes")
    public TeachingClass createClass(@Valid @RequestBody CreateClassRequest req) {
        TeachingClass clazz = new TeachingClass();
        Subject subj = new Subject(); 
        subj.setId(req.subjectId);
        clazz.setName(req.name);
        clazz.setSubject(subj);
        clazz.setCapacity(req.capacity);
        return classRepo.save(clazz);
    }

    @GetMapping("/classes")
    public List<TeachingClass> listClasses() {
        return classRepo.findAll();
    }

    // ===================== LỊCH / SLOT =====================
    @PostMapping("/schedule/slots")
    public ClassScheduleSlot addSlot(@Valid @RequestBody AddScheduleSlotRequest req) {
        return scheduleService.addSlot(req);
    }

    @GetMapping("/classes/{id}/slots")
    public List<ClassScheduleSlot> slots(@PathVariable Long id) {
        return scheduleService.byClass(id);
    }

 
    @PostMapping("/registrations/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId
    ) {
        approvalService.approveClassRegistration(adminId, id);
        return ResponseEntity.ok().build();
    }

  
    @PostMapping("/registrations/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.getOrDefault("reason", null) : null;
        approvalService.rejectClassRegistration(adminId, id, reason);
        return ResponseEntity.ok().build();
    }
}
