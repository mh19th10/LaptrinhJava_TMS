package vn.edu.uth.quanlidaythem.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.Subject;
import vn.edu.uth.quanlidaythem.repository.SubjectRepository;

/**
 * Tự động thêm các môn học cơ bản vào database khi ứng dụng khởi động
 * (nếu chưa có)
 */
@Configuration
public class SubjectDataInitializer {

    @Bean
    ApplicationRunner initializeSubjects(SubjectRepository subjectRepo) {
        return args -> {
            try {
                initializeSubjectsIfNeeded(subjectRepo);
            } catch (Exception e) {
                // Log lỗi nhưng không chặn app khởi động
                System.err.println("Lỗi khi khởi tạo dữ liệu môn học: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    @Transactional
    private void initializeSubjectsIfNeeded(SubjectRepository subjectRepo) {
        // Danh sách các môn học cần thêm
        String[][] subjects = {
            {"TOAN", "Toán học", ""},
            {"LY", "Vật lý", ""},
            {"HOA", "Hóa học", ""},
            {"ANH", "Tiếng Anh", ""},
            {"SINH", "Sinh học", ""},
            {"VAN", "Ngữ văn", ""},
            {"SU", "Lịch sử", ""},
            {"DIA", "Địa lý", ""},
            {"TIN", "Tin Học", ""}
        };

        int addedCount = 0;
        for (String[] subj : subjects) {
            String code = subj[0];
            String name = subj[1];
            String grade = subj[2];

            // Kiểm tra xem môn học đã tồn tại chưa
            if (!subjectRepo.existsByCode(code)) {
                Subject subject = new Subject();
                subject.setCode(code);
                subject.setName(name);
                subject.setGrade(grade != null && !grade.isEmpty() ? grade : null);
                subjectRepo.save(subject);
                addedCount++;
                System.out.println("✓ Đã thêm môn học: " + name + " (code: " + code + ")");
            } else {
                System.out.println("- Môn học đã tồn tại: " + name + " (code: " + code + ")");
            }
        }

        if (addedCount > 0) {
            System.out.println("✅ Đã khởi tạo " + addedCount + " môn học mới vào database.");
        } else {
            System.out.println("ℹ️ Tất cả môn học đã có trong database.");
        }
    }
}

