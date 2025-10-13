// assets/js/teacher.js
document.addEventListener('DOMContentLoaded', async () => {
  if (!requireLoginOrRedirect()) return;
  wireLogout();

  // Nếu trang là dashboard_teacher.html → gọi info để hiển thị tên
  if (location.pathname.endsWith('dashboard_teacher.html')) {
    try {
      const me = await fetchJSON('/api/teacher/info');
      const nameEl = document.getElementById('teacherName');
      if (nameEl) nameEl.textContent = `Chào, ${me.fullName || me.username || 'Giáo viên'}`;
      // ở đây bạn có thể gọi thêm API thống kê khi có
      // const stats = await fetchJSON('/api/teacher/stats');
    } catch (err) {
      console.error(err);
      alert('Không thể tải dữ liệu.');
    }
  }

  // Nếu trang là profile_teacher.html → nạp hồ sơ + submit cập nhật
  if (location.pathname.endsWith('profile_teacher.html')) {
    const fFullName = document.getElementById('fullName');
    const fEmail = document.getElementById('email');
    const fMainSubject = document.getElementById('mainSubject');
    const fPhone = document.getElementById('phone');
    const btn = document.getElementById('btnUpdate');

    async function loadProfile() {
      try {
        const me = await fetchJSON('/api/teacher/info');
        if (fFullName) fFullName.value = me.fullName || '';
        if (fEmail) fEmail.value = me.email || '';
        if (fMainSubject) fMainSubject.value = me.mainSubject || '';
        if (fPhone) fPhone.value = me.phone || '';
      } catch (e) {
        console.error(e);
        alert('Phiên hết hạn / lỗi tải hồ sơ');
        localStorage.clear();
        location.href = 'login.html';
      }
    }

    if (btn) {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
          await fetchJSON('/api/teacher/profile', {
            method: 'PUT',
            body: JSON.stringify({
              fullName: fFullName?.value?.trim() || null,
              email: fEmail?.value?.trim() || null,
              mainSubject: fMainSubject?.value?.trim() || null,
              phone: fPhone?.value?.trim() || null
            })
          });
          alert('Cập nhật thành công!');
          await loadProfile();
        } catch (e2) {
          console.error(e2);
          alert('Cập nhật thất bại!');
        }
      });
    }

    loadProfile();
  }
});
