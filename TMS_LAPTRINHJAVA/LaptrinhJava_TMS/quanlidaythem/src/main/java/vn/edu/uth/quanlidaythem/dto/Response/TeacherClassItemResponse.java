package vn.edu.uth.quanlidaythem.dto.Response;

public class TeacherClassItemResponse {
    private Long id;
    private String name;         // Tên lớp
    private String schedule;     // Ví dụ: "Thứ 4 - 18h00"
    private Integer studentCount;// Số học sinh
    private String nextTime;     // (tùy chọn)
    private String nextClassName;// (tùy chọn)

    public TeacherClassItemResponse() {}

    public TeacherClassItemResponse(Long id, String name, String schedule,
                                    Integer studentCount, String nextTime, String nextClassName) {
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.studentCount = studentCount;
        this.nextTime = nextTime;
        this.nextClassName = nextClassName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public String getNextTime() { return nextTime; }
    public void setNextTime(String nextTime) { this.nextTime = nextTime; }

    public String getNextClassName() { return nextClassName; }
    public void setNextClassName(String nextClassName) { this.nextClassName = nextClassName; }
}
