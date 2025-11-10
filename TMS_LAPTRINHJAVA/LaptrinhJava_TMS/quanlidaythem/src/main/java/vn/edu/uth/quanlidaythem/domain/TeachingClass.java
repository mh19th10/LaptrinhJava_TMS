//Đại diện lớp học phụ đạo/lớp dạy thêm
package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teaching_classes")
public class TeachingClass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name; // ví dụ: "Toán 9 - NC (Thứ 4 18h)"

    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "teacher_id")
    private Long teacherId; // được gán sau khi APPROVED

    public TeachingClass() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public Subject getSubject() { return subject; }
    public Integer getCapacity() { return capacity; }
    public Long getTeacherId() { return teacherId; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}