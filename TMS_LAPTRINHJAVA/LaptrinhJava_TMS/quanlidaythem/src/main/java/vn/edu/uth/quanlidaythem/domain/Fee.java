package vn.edu.uth.quanlidaythem.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "fees")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;
    private String className;
    private String month;
    private Long amount;
    private Long paid;
    private String dueDate;
    private String status;

    public Fee() {}

    public Fee(Long id, String studentName, String className, String month, Long amount, Long paid, String dueDate, String status) {
        this.id = id;
        this.studentName = studentName;
        this.className = className;
        this.month = month;
        this.amount = amount;
        this.paid = paid;
        this.dueDate = dueDate;
        this.status = status;
    }

    // getters & setters (generate all)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public Long getPaid() { return paid; }
    public void setPaid(Long paid) { this.paid = paid; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
