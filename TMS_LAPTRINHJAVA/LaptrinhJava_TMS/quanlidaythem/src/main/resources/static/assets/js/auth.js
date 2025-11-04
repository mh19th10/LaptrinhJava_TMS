document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('loginMessage'); // Giả sử bạn có một <div id="loginMessage"></div> để hiển thị thông báo

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            // Hiển thị thông báo đang xử lý và xóa thông báo cũ
            if (messageDiv) {
                messageDiv.textContent = 'Đang xử lý...';
                messageDiv.className = 'message info';
            }

            const data = {
                // Nên đổi id trong HTML thành "username" cho nhất quán
                username: document.getElementById('username').value,
                password: document.getElementById('password').value
            };

            try {
                // Dùng đường dẫn tương đối, không hardcode domain
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const res = await response.json();

                    // Lưu thông tin vào localStorage
                    localStorage.setItem('jwtToken', res.token); // Dùng jwtToken cho nhất quán
                    localStorage.setItem('userRole', res.role);
                    localStorage.setItem('username', res.username);

                    // Chuyển hướng đến trang đích
                    window.location.href = res.redirectUrl;

                } else {
                    const errorText = await response.text();
                    if (messageDiv) {
                        messageDiv.textContent = `Đăng nhập thất bại: ${errorText}`;
                        messageDiv.className = 'message error';
                    }
                }
            } catch (e) {
                console.error(e);
                if (messageDiv) {
                    messageDiv.textContent = 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
                    messageDiv.className = 'message error';
                }
            }
        });
    }
});