package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uth.quanlidaythem.domain.ApprovalLog;

public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Long> {}