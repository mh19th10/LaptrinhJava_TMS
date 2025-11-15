package vn.edu.uth.quanlidaythem.service;

import org.springframework.stereotype.Service;
import vn.edu.uth.quanlidaythem.repository.FeeRepository;
import vn.edu.uth.quanlidaythem.domain.Fee;

import java.util.List;

@Service
public class FeeService {

    private final FeeRepository feeRepository;

    public FeeService(FeeRepository feeRepository) {
        this.feeRepository = feeRepository;
    }

    public List<Fee> getAllFees() {
        return feeRepository.findAll();
    }

    public List<Fee> search(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        return feeRepository.findAll().stream()
                .filter(f ->
                        (f.getStudentName() != null && f.getStudentName().toLowerCase().contains(kw)) ||
                        (f.getClassName() != null && f.getClassName().toLowerCase().contains(kw))
                ).toList();
    }
}
