-- FIX: Chọn đúng schema trước khi INSERT
-- Lỗi: Table 'jpa_demo.fees' doesn't exist
-- Nguyên nhân: MySQL đang dùng schema mặc định là 'jpa_demo' thay vì 'tms'

-- CÁCH 1: Chọn schema trước (KHUYẾN NGHỊ)
USE tms;

INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) 
VALUES 
('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID'),
('Nguyễn Văn A', 'Văn luyện thi', 'Tháng 11/2024', 750000, 0, '2024-11-30', 'UNPAID'),
('Trần Thị B', 'Toán cơ bản 8', 'Tháng 11/2024', 600000, 300000, '2024-11-30', 'PARTIAL');

-- CÁCH 2: Chỉ định schema trong câu lệnh
INSERT INTO tms.fees (student_name, class_name, month, amount, paid, due_date, status) 
VALUES 
('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID');

-- Kiểm tra dữ liệu đã insert
SELECT * FROM tms.fees;

