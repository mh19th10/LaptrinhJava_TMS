# TMS – Teaching Management System

Hệ thống quản lý dạy thêm (Teaching Management System) xây dựng bằng **Spring Boot (Java)** và **MySQL**.

---

## 1. Chức năng chính

### Dành cho Admin
- Quản lý tài khoản người dùng (Admin/Teacher/Student).
- Duyệt / khóa tài khoản giáo viên.
- Quản lý môn học, lớp học.
- Xem danh sách đăng ký học.

### Dành cho Giáo viên
- Cập nhật hồ sơ cá nhân.
- Tạo lớp học, chỉnh sửa thông tin lớp.
- Xem danh sách học sinh đăng ký.
- Xác nhận / từ chối đăng ký học.

### Dành cho Học sinh / Phụ huynh
- Đăng ký tài khoản.
- Cập nhật thông tin cá nhân.
- Xem danh sách các lớp học.
- Gửi yêu cầu đăng ký lớp.
- Xem trạng thái đăng ký, kết quả học tập, học phí (nếu có).

---

## 2. Yêu cầu hệ thống

- **JDK**: 17
- **Maven**: 3.8+  
- **MySQL**: 8.x
- IDE khuyến nghị:
  - IntelliJ IDEA / Eclipse / VS Code (có plugin Java)

---

## 3. Cài đặt & chạy dự án (Developer)

### 3.1. Clone project


git clone https://github.com/mh19th10/LaptrinhJava_TMS.git
cd LaptrinhJava_TMS
// bước 1 : tạo Db trong Mysql
CREATE DATABASE tms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

// bước 2 :
Mở file src/main/resources/application.properties và kiểm tra cấu hình:

spring.datasource.url=jdbc:mysql://localhost:3306/tms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql=TRACE

// Bươc 3 : . Build & chạy
Cách 1 – dùng Maven:

mvn clean install
mvn spring-boot:run


Cách 2 – chạy trong IDE:

Mở project trong IntelliJ/Eclipse.

Tìm class QuanlidaythemApplication (hoặc class có annotation @SpringBootApplication).

Click chuột phải → Run.
6. Cấu trúc thư mục (tham khảo)
src/
 ├─ main/
 │   ├─ java/
 │   │   └─ vn/edu/uth/quanlidaythem/
 │   │        ├─ config/         # Cấu hình Spring Security, CORS,...
 │   │        ├─ controller/     # REST Controller / MVC Controller
 │   │        ├─ domain/         # Entity (User, Teacher, Student, Class, Enrollment,...)
 │   │        ├─ dto/            # DTO request/response
 │   │        ├─ repository/     # Repository (JPA)
 │   │        └─ service/        # Business logic
 │   └─ resources/
 │        ├─ templates/          # View (Thymeleaf) nếu có
 │        ├─ static/             # CSS, JS, image,...
 │        └─ application.properties
 └─ test/                        # Unit test (nếu có)

