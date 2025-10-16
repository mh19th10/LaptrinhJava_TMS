package vn.edu.uth.quanlidaythem.dto.Response;

public class TeacherProfileResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String mainSubject;

    public TeacherProfileResponse(Long id, String username, String fullName,
                                  String email, String phone, String mainSubject) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.mainSubject = mainSubject;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMainSubject() { return mainSubject; }
}
