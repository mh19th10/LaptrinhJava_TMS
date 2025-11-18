// ===============================
// tms-api.js (FIXED + COMPATIBLE)
// ===============================


const API_BASE_URL = "/api";

function wireLogout(buttonId = "logoutBtn") {
  const btn = document.getElementById(buttonId);
  if (!btn) return;
  btn.addEventListener("click", (ev) => {
    ev.preventDefault();
    logout();
  });
}


// ---- Auth Token ----
function getAuthToken() {
  try {
    return localStorage.getItem("authToken") || localStorage.getItem("jwtToken") || null;
  } catch {
    return null;
  }
}

function getAuthHeaders() {
  const token = getAuthToken();
  const authVal = token ? (token.startsWith("Bearer ") ? token : "Bearer " + token) : "";
  return {
    "Content-Type": "application/json",
    ...(authVal ? { "Authorization": authVal } : {})
  };
}

// ---- Convert snake_case → camelCase ----
function convertKeysToCamel(obj) {
  if (Array.isArray(obj)) {
    return obj.map(convertKeysToCamel);
  } else if (obj !== null && typeof obj === "object") {
    return Object.fromEntries(
      Object.entries(obj).map(([key, value]) => {
        const camelKey = key.replace(/_([a-zA-Z0-9])/g, (_, c) => c.toUpperCase());
        return [camelKey, convertKeysToCamel(value)];
      })
    );
  }
  return obj;
}

// ---- Universal Response Handler ----
async function handleResponse(res) {
  const status = res.status;
  const text = await res.text();
  let body = null;

  if (text) {
    try {
      body = JSON.parse(text);
    } catch (e) {
      // Not JSON — keep raw text
      body = text;
    }
  }

  if (!res.ok) {
    // Normalize some common server error shapes
    const message =
      (body && body.message) ||
      (body && body.error) ||
      (typeof body === "string" ? body : null) ||
      ("HTTP " + status);

    const err = new Error(message || ("HTTP " + status));
    err.status = status;
    err.body = body;
    throw err;
  }

  // If Spring pageable wrapper: { content: [...], totalElements:.., ... }
  if (body && typeof body === "object" && (body.content !== undefined)) {
    // convert content items to camelCase
    body.content = convertKeysToCamel(body.content);
    // Also convert other keys to camelCase
    const normalized = convertKeysToCamel(body);
    return normalized;
  }

  // If a wrapper { data: {...} } return data
  if (body && typeof body === "object" && body.data !== undefined) {
    return convertKeysToCamel(body.data);
  }

  // Otherwise convert keys and return
  return convertKeysToCamel(body);
}

// ---- fetchJSON wrapper (global) ----
async function fetchJSON(pathOrUrl, options = {}) {
  // Build full URL: if full URL provided, use it; if path starts with /api use as-is; otherwise prefix API_BASE_URL
  let url = pathOrUrl;
  if (!/^https?:\/\//i.test(pathOrUrl)) {
    if (pathOrUrl.startsWith("/")) {
      // If starts with /api use directly, else prefix API_BASE_URL (which is /api)
      if (pathOrUrl.startsWith("/api")) {
        url = pathOrUrl;
      } else {
        url = API_BASE_URL + pathOrUrl;
      }
    } else {
      url = API_BASE_URL + (pathOrUrl ? (pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl) : "");
    }
  }

  const method = (options.method || "GET").toUpperCase();
  const headers = { ...(getAuthHeaders() || {}), ...(options.headers || {}) };

  const fetchOpts = {
    method,
    headers
  };

  if (method !== "GET" && method !== "DELETE") {
    if (options.body !== undefined) {
      fetchOpts.body = (typeof options.body === "string") ? options.body : JSON.stringify(options.body);
    }
  }

  const res = await fetch(url, fetchOpts);

  // Map common statuses to messages expected by existing code
  if (res.status === 401) {
    const err = new Error("UNAUTH");
    err.status = 401;
    throw err;
  }
  if (res.status === 403) {
    const err = new Error("FORBIDDEN");
    err.status = 403;
    throw err;
  }

  return handleResponse(res);
}

// ---- Auth / RBAC helpers (global) ----
function requireLoginOrRedirect() {
  const token = getAuthToken();
  if (!token) {
    // not logged in
    try { alert("Bạn cần đăng nhập!"); } catch {}
    window.location.href = "/login.html";
    return false;
  }
  return true;
}

function requireRoleOrRedirect(expectedRole) {
  try {
    const userStr = localStorage.getItem("currentUser");
    if (!userStr) {
      // allow: frontend sometimes loads currentUser lazily; push to login for security
      try { alert("Bạn cần đăng nhập!"); } catch {}
      window.location.href = "/login.html";
      return false;
    }
    const user = JSON.parse(userStr || "{}");
    const role = (user.role || "").toUpperCase();
    if (!role || role !== String(expectedRole || "").toUpperCase()) {
      try { alert("Bạn không có quyền truy cập!"); } catch {}
      window.location.href = "/login.html";
      return false;
    }
    return true;
  } catch (e) {
    console.error("requireRoleOrRedirect error:", e);
    window.location.href = "/login.html";
    return false;
  }
}

function logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("currentUser");
    localStorage.clear();
    window.location.href = "/login.html";
}

function wireLogout(buttonId = "logoutBtn") {
  const btn = document.getElementById(buttonId);
  if (!btn) return;
  btn.addEventListener("click", (ev) => {
    ev.preventDefault();
    logout();
  });
}


// Expose helpers globally (for backward compatibility)
window.fetchJSON = fetchJSON;
window.requireLoginOrRedirect = requireLoginOrRedirect;
window.requireRoleOrRedirect = requireRoleOrRedirect;
window.wireLogout = wireLogout;
window.getAuthHeaders = getAuthHeaders;
window.getAuthToken = getAuthToken;

// ========================================================
//                    API DEFINITIONS
// (kept your original API object, unchanged in behavior)
// ========================================================

window.TMS_API = window.TMS_API || {

    // ---- Dashboard ----
    Dashboard: {
        getStatistics: async () => {
            const res = await fetch(`${API_BASE_URL}/dashboard/stats`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        getRecentActivities: async (limit = 8) => {
            const res = await fetch(`${API_BASE_URL}/dashboard/activities?limit=${limit}`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    },

    // ---- Admin ----
    Admin: {
        getOverviewStats: async () => {
            const res = await fetch(`/api/admin/stats/overview`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        getRecentActivities: async (limit = 10) => {
            const res = await fetch(`/api/admin/stats/recent-activities?limit=${limit}`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    },

    // ---- Classes ----
    Classes: {
        getAllAdmin: async () => {
            const res = await fetch(`/api/admin/classes?page=0&size=999`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        create: async (payload) => {
            const res = await fetch(`/api/classes`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(payload)
            });
            return handleResponse(res);
        },

        approveAdmin: async (id) => {
            const res = await fetch(`/api/admin/classes/${id}/approve`, {
                method: "PUT",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        rejectAdmin: async (id, reason) => {
            const res = await fetch(`/api/admin/classes/${id}/reject`, {
                method: "PUT",
                headers: getAuthHeaders(),
                body: JSON.stringify({ reason })
            });
            return handleResponse(res);
        }
    },

    // ---- Teachers ----
    Teachers: {
        getAll: async () => {
            const res = await fetch(`/api/admin/teachers`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        detail: async (id) => {
            const res = await fetch(`/api/admin/teachers/${id}`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        approve: async (id) => {
            const res = await fetch(`/api/admin/teachers/${id}/approve`, {
                method: "POST",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        reject: async (id, reason) => {
            const res = await fetch(`/api/admin/teachers/${id}/reject`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({ reason })
            });
            return handleResponse(res);
        },

        suspend: async (id, reason) => {
            const res = await fetch(`/api/admin/teachers/${id}/suspend`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({ reason })
            });
            return handleResponse(res);
        },

        revoke: async (id) => {
            const res = await fetch(`/api/admin/teachers/${id}/revoke`, {
                method: "POST",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    },

    // ---- Students ----
    Students: {
        getAvailableClasses: async () => {
            const res = await fetch(`/api/student/classes/available`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        getMyClasses: async () => {
            const res = await fetch(`/api/student/classes/my`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        registerClass: async (classId) => {
            const res = await fetch(`/api/student/classes/${classId}/register`, {
                method: "POST",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },

        unregisterClass: async (classId) => {
            const res = await fetch(`/api/student/classes/${classId}/register`, {
                method: "DELETE",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    },

    // ---- Fees ----
    Fees: {
        getAll: async () => {
            const res = await fetch(`${API_BASE_URL}/fees`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    },

    // ---- Payments ----
    Payments: {
        create: async (amount, feeId, studentName, notes) => {
            const res = await fetch(`${API_BASE_URL}/payments/create`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    amount: amount,
                    feeId: feeId,
                    studentName: studentName,
                    notes: notes
                })
            });
            return handleResponse(res);
        },
        getStatus: async (transactionId) => {
            const res = await fetch(`${API_BASE_URL}/payments/${transactionId}`, {
                method: "GET",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        },
        confirm: async (transactionId, status = "SUCCESS") => {
            const res = await fetch(`${API_BASE_URL}/payments/${transactionId}/confirm?status=${status}`, {
                method: "POST",
                headers: getAuthHeaders()
            });
            return handleResponse(res);
        }
    }

};


