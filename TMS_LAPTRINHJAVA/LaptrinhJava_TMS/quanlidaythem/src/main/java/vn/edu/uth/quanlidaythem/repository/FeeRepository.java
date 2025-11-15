package vn.edu.uth.quanlidaythem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uth.quanlidaythem.domain.Fee;

public interface FeeRepository extends JpaRepository<Fee, Long> {
}
