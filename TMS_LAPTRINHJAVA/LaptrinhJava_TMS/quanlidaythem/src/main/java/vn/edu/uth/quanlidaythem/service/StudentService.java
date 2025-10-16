package vn.edu.uth.quanlidaythem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateStudentProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfoResponse getInfo(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserInfoResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
    }

    public UserInfoResponse getProfile(String username) {
        return getInfo(username);
    }

    @Transactional
    public void updateProfile(String username, UpdateStudentProfileRequest req) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            u.setFullName(req.getFullName());
        }
        userRepository.save(u);
    }
}
