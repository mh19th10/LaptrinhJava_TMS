document.addEventListener('DOMContentLoaded', initTeacherDashboard);

async function initTeacherDashboard() {
  if (!requireLoginOrRedirect()) return;
  if (!requireRoleOrRedirect('TEACHER')) return; 
  wireLogout();

  // 1️⃣ Lấy thông tin giáo viên
  try {
    const me = await fetchJSON('/api/teachers/info');
    console.log('Teacher info:', me);
    const nameEl = document.getElementById('teacherName');
    if (nameEl) {
      const displayName = me.fullName || me.username || 'Giáo viên';
      nameEl.textContent = `Xin chào, ${displayName} 👋`;
      console.log('Updated teacher name to:', displayName);
    }
  } catch (e) {
    console.error('Error loading teacher info:', e);
    return handleAuthError(e);
  }

  // 2️⃣ Lấy danh sách lớp giảng dạy
  try {
    const classes = await fetchJSON('/api/teachers/classes');
    renderClassesTable(classes);
    setStatsFromClasses(classes);
  } catch (e) {
    handleAuthError(e);
    const tbody = document.getElementById('classesTbody');
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="4" style="color:#c00;text-align:center">
            Không thể tải danh sách lớp.
          </td>
        </tr>`;
    }
  }
}

function handleAuthError(e) {
  const msg = (e && e.message) ? e.message.toUpperCase() : '';
  if (msg === 'UNAUTHORIZED' || msg === 'UNAUTH') {
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

function renderClassesTable(classes = []) {
  const tbody = document.getElementById('classesTbody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (!Array.isArray(classes) || classes.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4" class="small-muted" style="text-align:center">
          Chưa có lớp nào.
        </td>
      </tr>`;
    return;
  }

  for (const c of classes) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${c.className ?? '--'}</td>
      <td>${formatSchedule(c.schedules)}</td>
      <td>${c.studentCount ?? (c.students?.length ?? '--')}</td>
      <td>
        <button class="btn small" data-id="${c.id ?? ''}" data-action="detail">Chi tiết</button>
      </td>`;
    tbody.appendChild(tr);
  }

  // Xử lý sự kiện click “Chi tiết”
  tbody.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="detail"]');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    alert('Mở chi tiết lớp ' + (id || ''));
    // location.href = `class_detail_teacher.html?id=${id}`; // tuỳ chọn thêm
  }, { once: true });
}

// Hàm định dạng lịch dạy
function formatSchedule(schedules = []) {
  if (!Array.isArray(schedules) || schedules.length === 0) return '--';
  return schedules.map(s => `${s.dayOfWeek} (${s.startTime} - ${s.endTime})`).join('<br>');
}

function setStatsFromClasses(classes = []) {
  const active = Array.isArray(classes) ? classes.length : 0;
  const students = classes.reduce((s, c) => s + (Number(c?.studentCount) || (c.students?.length ?? 0)), 0);
  const first = classes[0];

  const elActive = document.getElementById('statActiveClasses');
  const elStudents = document.getElementById('statStudents');
  const elNextTime = document.getElementById('statNextSession');
  const elNextName = document.getElementById('statNextClassName');

  if (elActive) elActive.textContent = String(active);
  if (elStudents) elStudents.textContent = String(students);
  if (elNextTime) elNextTime.textContent = first?.schedules?.[0]?.startTime || '--';
  if (elNextName) elNextName.textContent = first?.className || '--';
}
