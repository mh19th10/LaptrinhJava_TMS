-- Script tạo dữ liệu mẫu cho bảng fees
-- Chạy script này trong MySQL để tạo khoản nợ cho học sinh

-- Xóa dữ liệu cũ (nếu cần)
-- DELETE FROM fees;

-- Tạo các khoản nợ mẫu cho học sinh
INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) VALUES
-- Học phí chưa thanh toán
('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID'),
('Nguyễn Văn A', 'Văn luyện thi', 'Tháng 11/2024', 750000, 0, '2024-11-30', 'UNPAID'),

-- Học phí đã thanh toán một phần
('Trần Thị B', 'Toán cơ bản 8', 'Tháng 11/2024', 600000, 300000, '2024-11-30', 'PARTIAL'),

-- Học phí đã thanh toán đủ
('Lê Văn C', 'Anh văn giao tiếp', 'Tháng 11/2024', 800000, 800000, '2024-11-30', 'PAID'),

-- Học phí quá hạn
('Phạm Thị D', 'Lý nâng cao 10', 'Tháng 10/2024', 700000, 0, '2024-10-31', 'OVERDUE');

-- Kiểm tra dữ liệu đã tạo
SELECT * FROM fees ORDER BY id DESC;

-- Xem tổng số tiền nợ
SELECT 
    student_name,
    SUM(amount) as tong_tien,
    SUM(paid) as da_tra,
    SUM(amount - paid) as con_no
FROM fees
WHERE status IN ('UNPAID', 'PARTIAL', 'OVERDUE')
GROUP BY student_name;

