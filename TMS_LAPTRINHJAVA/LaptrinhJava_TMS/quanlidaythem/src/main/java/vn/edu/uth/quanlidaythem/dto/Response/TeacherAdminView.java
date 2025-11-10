// vn.edu.uth.quanlidaythem.dto.Response.TeacherAdminView
package vn.edu.uth.quanlidaythem.dto.Response;

import java.time.LocalDate;
import java.util.List;

public class TeacherAdminView {
    public Long id;
    public String name;
    public String email;
    public String phone;
    public LocalDate dob;
    public String degree;
    public Integer experience;
    public String address;
    public String bio;
    public String mainSubject;

    public boolean active;
    public String status;        // PENDING/APPROVED/REJECTED
    public String rejectReason;  // << thêm

    public List<SubjectPermissionView> subjects;

    public static class SubjectPermissionView {
        public Long id;
        public Long subjectId;
        public String code;
        public String name;
        public String grade;
        public boolean active;
        public String note;      // << nếu muốn hiện note từng môn
    }
}
