//Lưu tiến độ tối đa hiện tại của một môn ở trường 
package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "school_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id"}))
public class SchoolProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "max_week_allowed", nullable = false)
    private int maxWeekAllowed;

    public SchoolProgress() {}

    public Long getId() { return id; }
    public Subject getSubject() { return subject; }
    public int getMaxWeekAllowed() { return maxWeekAllowed; }
    public void setId(Long id) { this.id = id; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public void setMaxWeekAllowed(int maxWeekAllowed) { this.maxWeekAllowed = maxWeekAllowed; }
}