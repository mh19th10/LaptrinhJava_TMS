package vn.edu.uth.quanlidaythem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Response.TeacherClassItemResponse;
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

    // Mock dữ liệu để FE hiển thị (sau này thay bằng query DB thực)
    public List<TeacherClassItemResponse> getTeachingClasses(String username) {
        List<TeacherClassItemResponse> list = new ArrayList<>();
        list.add(new TeacherClassItemResponse(
                1L, "Toán 9 - Nâng cao", "Thứ 4 - 18h00", 20,
                "Thứ 4 - 18h00", "Toán 9 - Nâng cao"));
        list.add(new TeacherClassItemResponse(
                2L, "Vật lý 8", "Thứ 6 - 19h00", 25,
                null, null));
        return list;
    }
}
