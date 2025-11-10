// Đại diện môn học (Toán, Lý, Hóa…). Gồm code, name, grade. Dùng ở hầu hết luồng để ràng buộc lớp, topic, tiến độ.
package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 30)
    private String grade;

    public Subject() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setGrade(String grade) { this.grade = grade; }
}