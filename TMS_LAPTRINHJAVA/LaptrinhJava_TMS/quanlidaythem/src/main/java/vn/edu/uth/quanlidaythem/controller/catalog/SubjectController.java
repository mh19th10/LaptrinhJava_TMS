package vn.edu.uth.quanlidaythem.controller.catalog;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.uth.quanlidaythem.domain.CurriculumTopic;
import vn.edu.uth.quanlidaythem.domain.SchoolProgress;
import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.dto.Request.AddCurriculumTopicRequest;
import vn.edu.uth.quanlidaythem.dto.Request.CreateSubjectRequest;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateSchoolProgressRequest;
import vn.edu.uth.quanlidaythem.repository.SubjectRepository;
import vn.edu.uth.quanlidaythem.service.SubjectService;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjectService;
    private final SubjectRepository subjectRepo;

    public SubjectController(SubjectService s, SubjectRepository r){ this.subjectService=s; this.subjectRepo=r; }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Subject create(@Valid @RequestBody CreateSubjectRequest req){
        return subjectService.createSubject(req);
    }

    @GetMapping
    public List<Subject> list(){ return subjectRepo.findAll(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/topics")
    public CurriculumTopic addTopic(@Valid @RequestBody AddCurriculumTopicRequest req){
        return subjectService.addTopic(req);
    }

    @GetMapping("/{id}/topics")
    public List<CurriculumTopic> topics(@PathVariable Long id){
        return subjectService.topics(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/progress")
    public SchoolProgress upsertProgress(@Valid @RequestBody UpdateSchoolProgressRequest req){
        return subjectService.upsertProgress(req);
    }
}