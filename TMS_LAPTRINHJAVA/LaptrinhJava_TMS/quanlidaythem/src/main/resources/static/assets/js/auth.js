// ==================== AUTH.JS - KẾT NỐI API ====================
window.API_BASE_URL = 'http://localhost:8080/api';

// ==================== LOGIN ====================
document.addEventListener('DOMContentLoaded', function() {
  const loginForm = document.getElementById('loginForm');
  if (!loginForm) return;

  loginForm.addEventListener('submit', async function(e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !password) {
      alert('⚠️ Vui lòng nhập đầy đủ thông tin!');
      return;
    }

    const submitBtn = loginForm.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Đang đăng nhập...';
    submitBtn.disabled = true;

    try {
      console.log('🔐 Attempting login:', username);

      // POST body phù hợp với backend (LoginRequest: username + password)
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Đăng nhập thất bại');
      }

      const data = await response.json();

      console.log('✅ Login response:', data);
      console.log('🔑 Token:', data.token ? data.token.substring(0, 20) + '...' : 'NULL');
      console.log('🎯 Role:', data.role);
      console.log('📋 Roles:', data.roles);
      console.log('🔗 Redirect:', data.redirectUrl);

      // LƯU TOKEN (backend trả token field)
      if (!data.token) throw new Error('Server không trả token.');
      localStorage.setItem('authToken', data.token);

      // LƯU USER INFO (FE mong role không có prefix ROLE_, backend LoginResponse.getRole trả no-prefix)
      localStorage.setItem('currentUser', JSON.stringify({
        id: data.id,
        username: data.username,
        email: data.email || '',
        role: data.role || (Array.isArray(data.roles) && data.roles.length ? data.roles[0].replace('ROLE_', '') : 'STUDENT'),
        roles: data.roles || [],
        fullName: data.username
      }));

      // Redirect theo redirectUrl backend trả (LoginResponse.redirectUrl)
      window.location.href = data.redirectUrl || '/index.html';

    } catch (error) {
      console.error('❌ Login error:', error);
      alert('❌ ' + (error.message || error));
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    }
  });
});


// ==================== LOGOUT ====================
function logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("currentUser");
    localStorage.clear();
    window.location.href = "/login.html";
}


// ==================== CHECK AUTH ====================
function requireAuth() {
  let token = localStorage.getItem("authToken");
  let user = localStorage.getItem("currentUser");

  // Fix lỗi: dữ liệu lưu dưới dạng "undefined" hoặc "null"
  if (token === "undefined" || token === "null") token = null;
  if (user === "undefined" || user === "null") user = null;

  if (!token) {
    alert("Bạn cần đăng nhập để truy cập trang này!");
    window.location.href = "/login.html";
    return false;
  }

  // Nếu chỉ thiếu currentUser → tự load lại từ API, không đẩy về login
  if (!user) {
    console.warn("⚠️ currentUser không tồn tại. Thử load lại...");
    return true; // Vẫn cho vào trang
  }

  return true;
}


// ==================== GET AUTH HEADERS ====================
function getAuthHeaders() {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  };
}

// ==================== EXPORTS ====================
if (typeof window !== 'undefined') {
  window.logout = logout;
  window.requireAuth = requireAuth;
  window.getAuthHeaders = getAuthHeaders;
}