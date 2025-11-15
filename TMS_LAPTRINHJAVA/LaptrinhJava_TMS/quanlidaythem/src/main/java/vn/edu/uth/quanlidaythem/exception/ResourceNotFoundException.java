package vn.edu.uth.quanlidaythem.exception;

/**
 * Custom Exception cho trường hợp không tìm thấy resource
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
