//Nhật ký phê duyệt của Admin cho mỗi đăng ký
package vn.edu.uth.quanlidaythem.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "approval_logs")
public class ApprovalLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @ManyToOne(optional = false) @JoinColumn(name = "registration_id")
    private TeacherRegistration registration;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 50)
    private String action; // APPROVE/REJECT

    @Column(length = 200)
    private String reason;

    public ApprovalLog() {}

    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public TeacherRegistration getRegistration() { return registration; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAction() { return action; }
    public String getReason() { return reason; }
    public void setId(Long id) { this.id = id; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public void setRegistration(TeacherRegistration registration) { this.registration = registration; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setAction(String action) { this.action = action; }
    public void setReason(String reason) { this.reason = reason; }
}