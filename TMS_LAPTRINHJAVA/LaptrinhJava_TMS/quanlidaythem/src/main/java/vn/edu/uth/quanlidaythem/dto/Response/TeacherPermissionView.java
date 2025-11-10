package vn.edu.uth.quanlidaythem.dto.Response;

public class TeacherPermissionView {
    public Long id;

    // who
    public Long teacherId;
    public String teacherName;   // optional
    public String teacherEmail;  // optional

    // what subject
    public Long subjectId;
    public String subjectCode;
    public String subjectName;
    public String subjectGrade;

    // state
    public boolean active; // true = đã duyệt; false = pending/hủy
}
