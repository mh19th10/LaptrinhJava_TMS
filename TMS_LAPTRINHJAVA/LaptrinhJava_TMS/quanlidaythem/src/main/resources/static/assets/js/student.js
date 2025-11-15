document.addEventListener('DOMContentLoaded', initStudentDashboard);

async function initStudentDashboard() {
  if (!requireLoginOrRedirect()) return;
  if (!requireRoleOrRedirect('STUDENT')) return;

  wireLogout();

  try {
    const me = await fetchJSON('/api/student/info');
    document.getElementById('studentName').textContent = `Chào, ${me.fullName || me.username || 'Học sinh'} 👋`;
  } catch (e) {
    return handleAuthError(e);
  }

  try {
    const classes = await fetchJSON('/api/student/classes');
    renderStudentClasses(classes);
  } catch (e) {
    handleAuthError(e);
    const tbody = document.getElementById('studentClassesTbody');
    tbody.innerHTML = `<tr><td colspan="4" style="color:#c00;text-align:center">Không thể tải danh sách lớp.</td></tr>`;
  }
}

function renderStudentClasses(classes = []) {
  const tbody = document.getElementById('studentClassesTbody');
  tbody.innerHTML = '';

  if (!Array.isArray(classes) || classes.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;">Chưa có lớp nào.</td></tr>`;
    return;
  }

  for (const c of classes) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${c.className || '--'}</td>
      <td>${(typeof subjectLabel === 'function') ? subjectLabel(c.subject) : (c.subject || '--')}</td>
      <td>${c.teacher?.fullName || '--'}</td>
      <td>${(c.schedules && c.schedules.length)
            ? c.schedules.map(s => s.dayOfWeek).join(', ')
            : '--'}</td>`;
    tbody.appendChild(tr);
  }
}

function handleAuthError(e) {
  const msg = (e && e.message) ? e.message.toUpperCase() : '';
  if (msg === 'UNAUTH') {
    alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
    localStorage.clear();
    location.href = 'login.html';
  } else if (msg === 'FORBIDDEN') {
    alert('Bạn không có quyền truy cập.');
    location.href = 'login.html';
  } else {
    console.error(e);
  }
}

function subjectLabel(key) {
  const map = {
    'math': 'Toán học',
    'physics': 'Vật lý',
    'chemistry': 'Hóa học',
    'english': 'Tiếng Anh',
    'biology': 'Sinh học',
    'literature': 'Ngữ văn',
    'history': 'Lịch sử',
    'geography': 'Địa lý'
  };
  if (!key) return '';
  return map[key] || key;
}
