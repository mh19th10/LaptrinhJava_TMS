package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // ví dụ: MATH09
    private String name; // ví dụ: Toán 9
    private Integer gradeLevel; // ví dụ: 9

    public Subject() {}

    public Subject(String code, String name, Integer gradeLevel) {
        this.code = code;
        this.name = name;
        this.gradeLevel = gradeLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
}
