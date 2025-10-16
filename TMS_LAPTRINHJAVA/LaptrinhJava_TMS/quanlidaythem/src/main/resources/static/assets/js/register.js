document.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy ra form đăng ký bằng ID
    const registerForm = document.getElementById('registerForm');
    
    // Kiểm tra form có tồn tại không
    if (registerForm) {
        // 2. Lắng nghe sự kiện submit (nhấn nút Đăng ký)
        registerForm.addEventListener('submit', async function(event) {
            event.preventDefault(); // Ngăn trình duyệt gửi form theo cách truyền thống

            // 3. Thu thập dữ liệu từ các trường nhập liệu bằng ID
            const data = {
                // Lưu ý: Backend dùng 'username' (bạn đang dùng input 'email' làm username)
                username: document.getElementById('email').value, 
                password: document.getElementById('password').value,
                fullName: document.getElementById('fullname').value, 
                role: document.getElementById('role').value.toUpperCase() // Chuyển thành chữ HOA (HOC_SINH/GIAO_VIEN)
            };
            
            // Chuyển đổi role từ giá trị HTML sang giá trị DB (student -> HOC_SINH)
            const backendRole = (data.role === 'STUDENT') ? 'HOC_SINH' : (data.role === 'TEACHER' ? 'GIAO_VIEN' : 'HOC_SINH');

            const registrationData = {
                username: data.username,
                password: data.password,
                fullName: data.fullName,
                // Gán role đã được xử lý
                role: backendRole 
            };


            try {
                // 4. Gửi yêu cầu POST tới API Backend
                const response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(registrationData)
                });

                if (response.ok) {
                    alert('Đăng ký tài khoản thành công!');
                    // 5. Chuyển hướng nếu thành công
                    window.location.href = 'login.html'; 
                } else {
                    // Xử lý lỗi trả về từ Backend (Ví dụ: trùng username)
                    const errorText = await response.text();
                    alert('Đăng ký thất bại: ' + errorText);
                }
            } catch (error) {
                console.error('Lỗi kết nối Backend:', error);
                alert('Lỗi: Không thể kết nối đến máy chủ.');
            }
        });
    }
});