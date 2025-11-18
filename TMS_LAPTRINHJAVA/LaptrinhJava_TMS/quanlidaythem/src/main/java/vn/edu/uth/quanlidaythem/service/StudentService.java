package vn.edu.uth.quanlidaythem.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.dto.Request.UpdateStudentProfileRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ClassView;
import vn.edu.uth.quanlidaythem.dto.Response.StudentProfileResponse;
import vn.edu.uth.quanlidaythem.dto.Response.UserInfoResponse;
import vn.edu.uth.quanlidaythem.model.ClassEntity;
import vn.edu.uth.quanlidaythem.model.StudentEntity;
import vn.edu.uth.quanlidaythem.repository.ClassRepository;
import vn.edu.uth.quanlidaythem.repository.StudentRepository;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;

    public StudentService(UserRepository userRepository, StudentRepository studentRepository, ClassRepository classRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
    }

    public UserInfoResponse getInfo(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserInfoResponse(u.getId(), u.getUsername(), u.getFullName(), RoleUtils.normalize(u.getRole()));
    }

    public StudentProfileResponse getProfile(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Lấy StudentEntity (nếu có)
        StudentEntity student = null;
        if (u.getEmail() != null && !u.getEmail().isBlank()) {
            student = studentRepository.findByEmail(u.getEmail()).orElse(null);
        }
        
        StudentProfileResponse response = new StudentProfileResponse();
        response.setId(u.getId());
        response.setUsername(u.getUsername());
        response.setFullName(u.getFullName());
        response.setEmail(u.getEmail());
        response.setPhone(u.getPhone());
        response.setRole(RoleUtils.normalize(u.getRole()));
        
        // Thêm thông tin từ StudentEntity nếu có
        if (student != null) {
            response.setAddress(student.getAddress());
            response.setGrade(student.getGrade());
            response.setStudentType(student.getStudentType());
            // Ưu tiên thông tin từ StudentEntity
            if (student.getFullName() != null) response.setFullName(student.getFullName());
            if (student.getEmail() != null) response.setEmail(student.getEmail());
            if (student.getPhone() != null) response.setPhone(student.getPhone());
        }
        
        return response;
    }

    @Transactional
    public void updateProfile(String username, UpdateStudentProfileRequest req) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            u.setFullName(req.getFullName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            u.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            u.setPhone(req.getPhone());
        }
        userRepository.save(u);

        // Cập nhật StudentEntity nếu có
        StudentEntity student = null;
        if (u.getEmail() != null && !u.getEmail().isBlank()) {
            student = studentRepository.findByEmail(u.getEmail()).orElse(null);
        }
        
        if (student == null) {
            // Tạo mới StudentEntity nếu chưa có
            student = new StudentEntity();
            student.setFullName(u.getFullName());
            student.setEmail(u.getEmail() != null && !u.getEmail().isBlank() ? u.getEmail() : u.getUsername() + "@tms.local");
            student.setPhone(u.getPhone());
            student.setStatus("active");
        }
        
        // Cập nhật thông tin từ request
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            student.setFullName(req.getFullName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            student.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            student.setPhone(req.getPhone());
        }
        if (req.getAddress() != null) {
            student.setAddress(req.getAddress());
        }
        if (req.getGrade() != null) {
            student.setGrade(req.getGrade());
        }
        if (req.getStudentType() != null) {
            // Validate studentType
            String studentType = req.getStudentType().trim().toLowerCase();
            if (studentType.isEmpty() || 
                "failed".equals(studentType) || 
                "gifted".equals(studentType) || 
                "final-year".equals(studentType)) {
                student.setStudentType(studentType.isEmpty() ? null : studentType);
            }
            // Nếu không hợp lệ, không cập nhật (giữ nguyên giá trị cũ)
        }
        
        studentRepository.save(student);
    }

    /**
     * Lấy hoặc tạo StudentEntity từ User
     * Tìm kiếm theo cả email của user và username@tms.local để tránh tạo duplicate
     */
    private StudentEntity getOrCreateStudent(User user) {
        String usernameBasedEmail = user.getUsername() + "@tms.local";
        String userEmail = (user.getEmail() != null && !user.getEmail().isBlank()) 
            ? user.getEmail() 
            : null;
        
        // Bước 1: Tìm theo email của user (nếu có)
        if (userEmail != null) {
            Optional<StudentEntity> found = studentRepository.findByEmail(userEmail);
            if (found.isPresent()) {
                return found.get();
            }
        }
        
        // Bước 2: Tìm theo username@tms.local (trường hợp student đã được tạo trước đó)
        Optional<StudentEntity> found = studentRepository.findByEmail(usernameBasedEmail);
        if (found.isPresent()) {
            StudentEntity student = found.get();
            // Nếu user có email và email này chưa được sử dụng bởi student khác, cập nhật
            if (userEmail != null && !userEmail.equals(student.getEmail())) {
                // Kiểm tra xem email của user có bị trùng với student khác không
                Optional<StudentEntity> emailCheck = studentRepository.findByEmail(userEmail);
                if (emailCheck.isEmpty()) {
                    // Chỉ cập nhật email nếu không bị trùng
                    student.setEmail(userEmail);
                    return studentRepository.save(student);
                }
                // Nếu email đã tồn tại, giữ nguyên student hiện tại
            }
            return student;
        }
        
        // Bước 3: Tạo mới student
        StudentEntity student = new StudentEntity();
        student.setFullName(user.getFullName());
        student.setEmail(userEmail != null ? userEmail : usernameBasedEmail);
        student.setPhone(user.getPhone());
        student.setStatus("active");
        
        try {
            return studentRepository.save(student);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Nếu bị duplicate key (có thể do race condition), thử tìm lại
            if (userEmail != null) {
                found = studentRepository.findByEmail(userEmail);
                if (found.isPresent()) {
                    return found.get();
                }
            }
            found = studentRepository.findByEmail(usernameBasedEmail);
            if (found.isPresent()) {
                return found.get();
            }
            // Nếu vẫn không tìm thấy, throw lại exception
            throw e;
        }
    }

    /**
     * Lấy danh sách lớp học cho học sinh đăng ký (chỉ lớp đã được duyệt)
     * Tất cả học sinh đều có thể đăng ký tất cả lớp (đã bỏ validation studentType)
     */
    @Transactional(readOnly = true)
    public List<ClassView> getAvailableClasses(String username) {
        // Validate user tồn tại
        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        
        // Lấy tất cả lớp có status = "approved"
        List<ClassEntity> allClasses = classRepository.findByStatus("approved");
        
        // Map tất cả lớp - TẤT CẢ học sinh đều có thể đăng ký
        return allClasses.stream()
                .map(cls -> {
                    ClassView view = mapToClassView(cls);
                    view.canRegister = true; // Tất cả đều có thể đăng ký
                    view.registrationMessage = null;
                    return view;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lớp học sinh đã đăng ký
     */
    @Transactional(readOnly = true)
    public List<ClassView> getMyClasses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        
        // Chỉ tìm StudentEntity, không tạo mới (vì đây là read-only)
        StudentEntity student = null;
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            student = studentRepository.findByEmail(user.getEmail()).orElse(null);
        }
        
        // Nếu chưa có StudentEntity, trả về danh sách rỗng (chưa đăng ký lớp nào)
        if (student == null) {
            return new ArrayList<>();
        }
        
        return student.getClasses().stream()
                .map(this::mapToClassView)
                .collect(Collectors.toList());
    }

    /**
     * Đăng ký lớp học
     */
    @Transactional
    public ClassView registerClass(String username, Long classId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học"));
        
        // Kiểm tra lớp đã được duyệt chưa
        if (!"approved".equalsIgnoreCase(classEntity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học chưa được duyệt, không thể đăng ký");
        }
        
        StudentEntity student = getOrCreateStudent(user);
        
        // Kiểm tra học sinh đã đăng ký lớp này chưa
        if (student.getClasses().contains(classEntity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn đã đăng ký lớp này rồi");
        }
        
        // ĐÃ BỎ VALIDATION STUDENTTYPE - Tất cả học sinh đều có thể đăng ký
        
        // Thêm học sinh vào lớp
        student.getClasses().add(classEntity);
        studentRepository.save(student);
        
        return mapToClassView(classEntity);
    }

    /**
     * Hủy đăng ký lớp học
     */
    @Transactional
    public void unregisterClass(String username, Long classId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học"));
        
        StudentEntity student = getOrCreateStudent(user);
        
        if (!student.getClasses().contains(classEntity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn chưa đăng ký lớp này");
        }
        
        student.getClasses().remove(classEntity);
        studentRepository.save(student);
    }

    /**
     * Map ClassEntity sang ClassView DTO
     */
    private ClassView mapToClassView(ClassEntity classEntity) {
        ClassView view = new ClassView();
        view.id = classEntity.getId();
        view.className = classEntity.getClassName();
        view.subject = classEntity.getSubject();
        view.type = classEntity.getType();
        view.status = classEntity.getStatus();
        view.studentCount = classEntity.getStudentCount();
        if (classEntity.getTeacher() != null) {
            view.teacherName = classEntity.getTeacher().getFullName();
        }
        // canRegister và registrationMessage sẽ được set ở getAvailableClasses()
        view.canRegister = true; // Default
        view.registrationMessage = null;
        return view;
    }
}
