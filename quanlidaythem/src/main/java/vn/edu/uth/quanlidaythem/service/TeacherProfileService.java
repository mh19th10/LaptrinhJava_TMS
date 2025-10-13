package vn.edu.uth.quanlidaythem.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateTeacherProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherProfileResponse;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class TeacherProfileService {

    private final UserRepository userRepository;

    public TeacherProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String username = a.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public TeacherProfileResponse getProfile() {
        User u = currentUser();
        return new TeacherProfileResponse(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getMainSubject()
        );
    }

    @Transactional
    public TeacherProfileResponse updateProfile(UpdateTeacherProfileRequest req) {
        User u = currentUser();
        if (req.getFullName() != null)    u.setFullName(req.getFullName());
        if (req.getEmail() != null)       u.setEmail(req.getEmail());
        if (req.getPhone() != null)       u.setPhone(req.getPhone());
        if (req.getMainSubject() != null) u.setMainSubject(req.getMainSubject());
        userRepository.save(u);
        return getProfile();
    }
}
