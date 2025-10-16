// assets/js/teacher.js
document.addEventListener('DOMContentLoaded', initTeacherDashboard);

// Nếu bạn CHƯA có requireRoleOrRedirect trong api.js, dùng hàm này
function ensureRole(required) {
  const role = (localStorage.getItem('userRole') || '').toUpperCase();
  if (role !== required) {
    alert('Bạn không có quyền truy cập trang này.');
    // nếu là học sinh thì đẩy về dashboard học sinh, còn lại về login
    location.href = role === 'STUDENT' ? 'dashboard_student.html' : 'login.html';
    return false;
  }
  return true;
}

async function initTeacherDashboard() {
  // requireLoginOrRedirect() đến từ assets/js/api.js của bạn
  if (!requireLoginOrRedirect()) return;

  // nếu có sẵn requireRoleOrRedirect thì dùng; không thì fallback ensureRole
  if (typeof requireRoleOrRedirect === 'function') {
    if (!requireRoleOrRedirect('TEACHER')) return;
  } else {
    if (!ensureRole('TEACHER')) return;
  }

  wireLogout();

  // 1) Tải thông tin giáo viên
  try {
    const me = await fetchJSON('/api/teacher/info');
    const nameEl = document.getElementById('teacherName');
    if (nameEl) nameEl.textContent = `Chào, ${me.fullName || me.username || 'Giáo viên'} 👋`;
  } catch (e) {
    return handleAuthError(e);
  }

  // 2) Tải danh sách lớp giảng dạy
  try {
    const classes = await fetchJSON('/api/teacher/classes'); // cần BE cung cấp
    renderClassesTable(classes);
    setStatsFromClasses(classes);
  } catch (e) {
    handleAuthError(e);
    const tbody = document.getElementById('classesTbody');
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="4" class="small-muted" style="color:#c00;text-align:center">
            Không thể tải danh sách lớp.
          </td>
        </tr>`;
    }
  }
}

function handleAuthError(e) {
  // Với api.js hiện tại bạn ném 'UNAUTHORIZED' cho 401/403
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
      <td>${c?.name ?? '--'}</td>
      <td>${c?.schedule ?? '--'}</td>
      <td>${c?.studentCount ?? '--'}</td>
      <td>
        <button class="btn small" data-id="${c?.id ?? ''}" data-action="detail">Chi tiết</button>
      </td>`;
    tbody.appendChild(tr);
  }

  // Demo click "Chi tiết"
  tbody.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="detail"]');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    alert('Mở chi tiết lớp ' + (id || ''));
    // location.href = `class_detail_teacher.html?id=${id}`; // nếu có trang chi tiết
  }, { once: true });
}

function setStatsFromClasses(classes = []) {
  const active   = Array.isArray(classes) ? classes.length : 0;
  const students = classes.reduce((s, c) => s + (Number(c?.studentCount) || 0), 0);
  const first    = classes[0];

  const elActive     = document.getElementById('statActiveClasses');
  const elStudents   = document.getElementById('statStudents');
  const elNextTime   = document.getElementById('statNextSession');
  const elNextName   = document.getElementById('statNextClassName');

  if (elActive)   elActive.textContent   = String(active);
  if (elStudents) elStudents.textContent = String(students);
  if (elNextTime) elNextTime.textContent = first?.nextTime || first?.schedule || '--';
  if (elNextName) elNextName.textContent = first?.nextClassName || first?.name || '--';
}
