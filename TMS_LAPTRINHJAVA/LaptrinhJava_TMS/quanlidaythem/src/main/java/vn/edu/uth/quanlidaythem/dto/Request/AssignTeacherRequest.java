package vn.edu.uth.quanlidaythem.dto.Request;

public class AssignTeacherRequest {
    private Long classId;
    private Long teacherUserId;

    public AssignTeacherRequest() {}

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(Long teacherUserId) { this.teacherUserId = teacherUserId; }
}