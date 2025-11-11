// ===== Auth helpers =====
function getToken() {
  return localStorage.getItem('jwtToken');
}
function getRole() {
  return (localStorage.getItem('userRole') || '').toUpperCase();
}

async function fetchJSON(url, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set('Accept', 'application/json');
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const token = getToken();
  if (token) headers.set('Authorization', 'Bearer ' + token);

  const res = await fetch(url, { ...options, headers });

  if (res.status === 401) throw new Error('UNAUTH');
  if (res.status === 403) throw new Error('FORBIDDEN');
  if (res.status === 204) return null; // no content

  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `HTTP_${res.status}`);
  }

  const ct = (res.headers.get('content-type') || '').toLowerCase();
  return ct.includes('application/json') ? res.json() : res.text();
}

function requireLoginOrRedirect() {
  if (!getToken()) {
    alert('Bạn cần đăng nhập!');
    location.href = 'login.html';
    return false;
  }
  return true;
}
function requireRoleOrRedirect(role) {
  if (getRole() !== role.toUpperCase()) {
    if (role === 'TEACHER' && getRole() === 'STUDENT') {
      location.href = 'dashboard_student.html';
    } else {
      location.href = 'login.html';
    }
    return false;
  }
  return true;
}
function wireLogout(anchorId = 'logoutBtn') {
  const a = document.getElementById(anchorId);
  if (!a) return;
  a.addEventListener('click', (e) => {
    e.preventDefault();
    localStorage.clear();
    location.href = 'login.html';
  });
}

// ===== SUBJECTS =====
async function listSubjectsApi() {
  return await fetchJSON('/api/subjects');
}

// ===== TEACHER PERMISSIONS (đăng ký môn) =====
async function listMyPermissionsApi() {
  return await fetchJSON('/api/teacher/permissions');
}
async function createPermissionApi(subjectId) {
  return await fetchJSON('/api/teacher/permissions', {
    method: 'POST',
    body: JSON.stringify({ subjectId })
  });
}
async function revokePermissionApi(permId) {
  return await fetchJSON(`/api/teacher/permissions/${permId}`, { method: 'DELETE' });
}

// ===== ADMIN TEACHERS (tùy chọn) =====
async function listTeachersApi({ status, subject, q } = {}) {
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  if (subject) params.set('subject', subject);
  if (q) params.set('q', q);
  const qs = params.toString() ? '?' + params.toString() : '';
  return await fetchJSON('/api/admin/teachers' + qs);
}
async function getTeacherApi(id) {
  return await fetchJSON(`/api/admin/teachers/${id}`);
}
async function approveTeacherApi(id) {
  return await fetchJSON(`/api/admin/teachers/${id}/approve`, { method: 'POST' });
}
async function rejectTeacherApi(id, reason) {
  const body = reason ? { reason } : {};
  return await fetchJSON(`/api/admin/teachers/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify(body)
  });
}
async function suspendTeacherApi(id, reason) {
  const body = reason ? { reason } : {};
  return await fetchJSON(`/api/admin/teachers/${id}/suspend`, {
    method: 'POST',
    body: JSON.stringify(body)
  });
}

// ===================== QUẢN LÝ LỚP (kết nối BE) =====================
// Lấy danh sách lớp admin đã tạo sẵn (để giáo viên đăng ký dạy)
async function listClassesApi() {
  return await fetchJSON('/api/admin/teach/classes');
}
// Lấy slot lịch của 1 lớp (nếu cần hiển thị)
async function listClassSlotsApi(classId) {
  return await fetchJSON(`/api/admin/teach/classes/${classId}/slots`);
}

// ===================== TEACHER REGISTRATIONS (Đăng ký LỚP) =====================
// Lấy danh sách yêu cầu đăng ký lớp của chính giáo viên
async function listMyRegistrationsApi() {
  return await fetchJSON('/api/teacher/registrations');
}
// Đăng ký dạy một lớp có sẵn (truyền classId)
async function registerIntoClassApi(classId) {
  return await fetchJSON('/api/teacher/registrations', {
    method: 'POST',
    body: JSON.stringify({ classId })
  });
}
// Đề nghị mở lớp mới (CUSTOM) – KHÔNG gửi classId
// payload: { className, subjectId, capacity, mode, startDate, location, schedule, note }
async function registerCustomClassApi(payload) {
  return await fetchJSON('/api/teacher/registrations/custom', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
// Hủy yêu cầu (chỉ khi đang PENDING)
async function cancelRegistrationApi(registrationId) {
  return await fetchJSON(`/api/teacher/registrations/${registrationId}`, { method: 'DELETE' });
}

// (Optional) Admin duyệt/từ chối đăng ký lớp
async function adminApproveRegistrationApi(regId, adminId) {
  return await fetchJSON(`/api/admin/teach/registrations/${regId}/approve`, {
    method: 'POST',
    headers: { 'X-Admin-Id': String(adminId) }
  });
}
async function adminRejectRegistrationApi(regId, adminId, reason) {
  return await fetchJSON(`/api/admin/teach/registrations/${regId}/reject`, {
    method: 'POST',
    headers: { 'X-Admin-Id': String(adminId) },
    body: JSON.stringify({ reason })
  });
}
