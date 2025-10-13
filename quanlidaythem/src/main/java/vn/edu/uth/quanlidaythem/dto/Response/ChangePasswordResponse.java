package vn.edu.uth.quanlidaythem.dto.Response;

public class ChangePasswordResponse {
    private boolean success;
    private String message;

    public ChangePasswordResponse() {}

    public ChangePasswordResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // tiện dùng nhanh
    public static ChangePasswordResponse ok(String message) {
        return new ChangePasswordResponse(true, message);
    }
    public static ChangePasswordResponse fail(String message) {
        return new ChangePasswordResponse(false, message);
    }
}
