package vn.edu.uth.quanlidaythem.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.uth.quanlidaythem.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); 
    // Phương thức kiểm tra Username đã tồn tại (cần cho Đăng ký)
    Boolean existsByUsername(String username);
}