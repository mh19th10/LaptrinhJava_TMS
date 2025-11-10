package vn.edu.uth.quanlidaythem.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.CurriculumTopic;
import vn.edu.uth.quanlidaythem.domain.SchoolProgress;
import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.dto.Request.AddCurriculumTopicRequest;
import vn.edu.uth.quanlidaythem.dto.Request.CreateSubjectRequest;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateSchoolProgressRequest;
import vn.edu.uth.quanlidaythem.repository.CurriculumTopicRepository;
import vn.edu.uth.quanlidaythem.repository.SchoolProgressRepository;
import vn.edu.uth.quanlidaythem.repository.SubjectRepository;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepo;
    private final CurriculumTopicRepository topicRepo;
    private final SchoolProgressRepository progressRepo;

    public SubjectService(SubjectRepository s, CurriculumTopicRepository t, SchoolProgressRepository p) {
        this.subjectRepo = s; this.topicRepo = t; this.progressRepo = p;
    }

    @Transactional
    public Subject createSubject(CreateSubjectRequest req) {
        if (subjectRepo.existsByCode(req.code)) throw new IllegalArgumentException("Mã môn đã tồn tại");
        Subject s = new Subject();
        s.setCode(req.code); s.setName(req.name); s.setGrade(req.grade);
        return subjectRepo.save(s);
    }

    @Transactional
    public CurriculumTopic addTopic(AddCurriculumTopicRequest req) {
        Subject s = subjectRepo.findById(req.subjectId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học"));
        CurriculumTopic t = new CurriculumTopic();
        t.setSubject(s); t.setWeekIndex(req.weekIndex); t.setTitle(req.title);
        return topicRepo.save(t);
    }

    @Transactional
    public SchoolProgress upsertProgress(UpdateSchoolProgressRequest req) {
        Subject s = subjectRepo.findById(req.subjectId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học"));
        SchoolProgress p = progressRepo.findBySubject_Id(s.getId()).orElseGet(SchoolProgress::new);
        p.setSubject(s); p.setMaxWeekAllowed(req.maxWeekAllowed);
        return progressRepo.save(p);
    }

    public List<CurriculumTopic> topics(Long subjectId){
        return topicRepo.findBySubject_IdOrderByWeekIndexAsc(subjectId);
    }
}