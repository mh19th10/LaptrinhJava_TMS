// assets/js/api.js
function getToken() {
  return localStorage.getItem('jwtToken'); // được set sau khi login
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
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `HTTP_${res.status}`);
  }
  const ct = res.headers.get('content-type') || '';
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
