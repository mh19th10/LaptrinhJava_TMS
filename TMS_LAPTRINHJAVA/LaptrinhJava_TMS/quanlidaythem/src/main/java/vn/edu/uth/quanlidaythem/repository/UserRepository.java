package vn.edu.uth.quanlidaythem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.uth.quanlidaythem.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);

    // Để AuthService dùng khi đăng ký tài khoản
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    long countByRoleIgnoreCase(String role);
    long countByRoleIgnoreCaseAndStatusIgnoreCase(String role, String status);
}

