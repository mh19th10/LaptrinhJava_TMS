document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('btnChangePwd');
  if (!btn) return;

  btn.addEventListener('click', async (e) => {
    e.preventDefault();

    const token = localStorage.getItem('jwtToken');
    if (!token) { alert('Bạn cần đăng nhập lại.'); location.href = 'login.html'; return; }

    const body = {
      oldPassword: (document.getElementById('oldPassword')?.value || '').trim(),
      newPassword: (document.getElementById('newPassword')?.value || '').trim(),
      confirmPassword: (document.getElementById('confirmPassword')?.value || '').trim(),
    };

    if (!body.oldPassword || !body.newPassword || !body.confirmPassword) {
      alert('Vui lòng nhập đủ 3 ô mật khẩu.');
      return;
    }
    if (body.newPassword !== body.confirmPassword) {
      alert('Xác nhận mật khẩu không khớp.');
      return;
    }

    try {
      const res = await fetch('/api/account/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify(body)
      });
      const data = await res.json().catch(() => ({}));

      if (!res.ok || data.success === false) {
        alert(data.message || 'Đổi mật khẩu thất bại.');
        return;
      }

      alert(data.message || 'Đổi mật khẩu thành công! Vui lòng đăng nhập lại.');
      localStorage.clear();
      location.href = 'login.html';
    } catch (err) {
      console.error(err);
      alert('Có lỗi mạng khi đổi mật khẩu.');
    }
  });
});
