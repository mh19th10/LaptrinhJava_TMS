package vn.edu.uth.quanlidaythem.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateTeacherProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherProfileResponse;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class TeacherProfileService {

    private final UserRepository userRepo;

    public TeacherProfileService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("UNAUTHENTICATED");
        return auth.getName();
    }

    private TeacherProfileResponse mapToResponse(User u, boolean success, String message) {
        TeacherProfileResponse res = new TeacherProfileResponse();
        res.setSuccess(success);
        res.setMessage(message);

        res.setFullName(u.getFullName());
        res.setUsername(u.getUsername());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setMainSubject(u.getMainSubject());

        res.setDob(u.getDob());
        res.setDegree(u.getDegree());
        res.setExperience(u.getExperience());
        res.setAddress(u.getAddress());
        res.setBio(u.getBio());
        return res;
    }

    // GET /api/teacher/profile
    public TeacherProfileResponse getProfile() {
        String username = currentUsername();
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên: " + username));
        return mapToResponse(u, true, "OK");
    }

    // PUT /api/teacher/profile
    public TeacherProfileResponse updateProfile(UpdateTeacherProfileRequest req) {
        String username = currentUsername();
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên: " + username));

        try {
            if (req.getFullName() != null) u.setFullName(req.getFullName());
            if (req.getPhone() != null) u.setPhone(req.getPhone());
            if (req.getMainSubject() != null) u.setMainSubject(req.getMainSubject());

            if (req.getDob() != null) u.setDob(req.getDob());
            if (req.getDegree() != null) u.setDegree(req.getDegree());
            if (req.getExperience() != null) u.setExperience(req.getExperience());
            if (req.getAddress() != null) u.setAddress(req.getAddress());
            if (req.getBio() != null) u.setBio(req.getBio());

            userRepo.save(u);
            return mapToResponse(u, true, "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            TeacherProfileResponse fail = new TeacherProfileResponse();
            fail.setSuccess(false);
            fail.setMessage("Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            return fail;
        }
    }
}
