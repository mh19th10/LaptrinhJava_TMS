//kiểm tra quy định TT29: không dạy trước chương trình.
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
@Table(name = "curriculum_topics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id","week_index"}))
public class CurriculumTopic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "week_index", nullable = false)
    private int weekIndex;

    @Column(nullable = false, length = 200)
    private String title;

    public CurriculumTopic() {}

    public Long getId() { return id; }
    public Subject getSubject() { return subject; }
    public int getWeekIndex() { return weekIndex; }
    public String getTitle() { return title; }
    public void setId(Long id) { this.id = id; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public void setWeekIndex(int weekIndex) { this.weekIndex = weekIndex; }
    public void setTitle(String title) { this.title = title; }
}