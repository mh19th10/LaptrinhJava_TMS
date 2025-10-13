// assets/js/api.js
function getToken() {
  return localStorage.getItem('jwtToken'); // bạn đang lưu key này từ bước login
}

async function fetchJSON(url, options = {}) {
  const token = getToken();
  const headers = new Headers(options.headers || {});
  headers.set('Accept', 'application/json');
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', 'Bearer ' + token);
  }

  const res = await fetch(url, { ...options, headers });
  if (res.status === 401 || res.status === 403) {
    throw new Error('UNAUTHORIZED');
  }
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `HTTP ${res.status}`);
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return res.text();
}

function requireLoginOrRedirect() {
  if (!getToken()) {
    alert('Bạn cần đăng nhập!');
    window.location.href = 'login.html';
    return false;
  }
  return true;
}

function wireLogout(anchorId = 'logoutBtn') {
  const a = document.getElementById(anchorId);
  if (a) {
    a.addEventListener('click', (e) => {
      e.preventDefault();
      localStorage.clear();
      window.location.href = 'login.html';
    });
  }
}
