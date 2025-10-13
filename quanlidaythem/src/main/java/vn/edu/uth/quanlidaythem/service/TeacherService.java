package vn.edu.uth.quanlidaythem.service;

import org.springframework.stereotype.Service;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class TeacherService {

    private final UserRepository userRepository;

    public TeacherService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfoResponse getInfo(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserInfoResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
    }
}
