package vn.edu.uth.quanlidaythem.controller.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.uth.quanlidaythem.domain.ClassScheduleSlot;
import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.domain.TeachingClass;
import vn.edu.uth.quanlidaythem.dto.Request.AddScheduleSlotRequest;
import vn.edu.uth.quanlidaythem.dto.Request.CreateClassRequest;
import vn.edu.uth.quanlidaythem.repository.TeachingClassRepository;
import vn.edu.uth.quanlidaythem.service.ScheduleService;

@RestController
@RequestMapping("/api/admin/teach")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeachingController {
    private final TeachingClassRepository classRepo;
    private final ScheduleService scheduleService;

    public AdminTeachingController(TeachingClassRepository c, ScheduleService s){
        this.classRepo=c; this.scheduleService=s;
    }

    // ===== LỚP =====
    @PostMapping("/classes")
    public TeachingClass createClass(@Valid @RequestBody CreateClassRequest req){
        TeachingClass clazz = new TeachingClass();
        Subject subj = new Subject(); subj.setId(req.subjectId);
        clazz.setName(req.name); clazz.setSubject(subj); clazz.setCapacity(req.capacity);
        return classRepo.save(clazz);
    }

    @GetMapping("/classes")
    public List<TeachingClass> listClasses(){ return classRepo.findAll(); }

    // ===== LỊCH / SLOT =====
    @PostMapping("/schedule/slots")
    public ClassScheduleSlot addSlot(@Valid @RequestBody AddScheduleSlotRequest req){
        return scheduleService.addSlot(req);
    }

    @GetMapping("/classes/{id}/slots")
    public List<ClassScheduleSlot> slots(@PathVariable Long id){
        return scheduleService.byClass(id);
    }

    // 👉 BỎ HẲN 2 endpoint dưới vì đã chuyển sang duyệt quyền môn:
    //    POST /api/admin/permissions  (cấp/duyệt)
    //    PUT  /api/admin/permissions/{id}/active  (bật/tắt)
}
