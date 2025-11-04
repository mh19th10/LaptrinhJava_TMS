package vn.edu.uth.quanlidaythem.dto.Response;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String role;
    private String redirectUrl;

    public LoginResponse(String token, Long id, String username, String role, String redirectUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.role = role;
        this.redirectUrl = redirectUrl;
    }

    // Getters & Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}
