// ==================== API CLIENT ====================
const handleResponse = async (response) => {
    if (response.status === 401 || response.status === 403) {
        throw new Error("UNAUTH");
    }

    if (response.status === 204) return null;

    if (!response.ok) {
        const text = await response.text().catch(() => "");
        throw new Error(text || `HTTP ${response.status}`);
    }

    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }
    return response.text();
};


// ==================== TMS API WRAPPER ====================
const TMS_API = {

    // -------- AUTH --------
    Auth: {
        login: async (username, password) => {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });
            return handleResponse(response);
        },

        logout: () => {
            localStorage.removeItem("authToken");
            localStorage.removeItem("currentUser");
            window.location.href = "login.html";
        }
    },

    // -------- DASHBOARD --------
    Dashboard: {
        getStatistics: async () => {
            const response = await fetch(`${API_BASE_URL}/admin/stats/overview`, {
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        },

        getRecentActivities: async (limit = 10) => {
            const response = await fetch(`${API_BASE_URL}/admin/stats/recent-activities?limit=${limit}`, {
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        }
    },

    // -------- CLASSES --------
    Classes: {
        getAll: async (page = 0, size = 20, status = "", type = "") => {
            let url = `${API_BASE_URL}/admin/classes?page=${page}&size=${size}`;
            if (status) url += `&status=${status}`;
            if (type) url += `&type=${type}`;

            const response = await fetch(url, {
                headers: getAuthHeaders()
            });
            const result = await handleResponse(response);
            return result.content || result;
        },

        getById: async (id) => {
            const response = await fetch(`${API_BASE_URL}/admin/classes/${id}`, {
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        },

        create: async (data) => {
            const response = await fetch(`${API_BASE_URL}/admin/classes`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(data)
            });
            return handleResponse(response);
        },

        approve: async (id) => {
            const response = await fetch(`${API_BASE_URL}/admin/classes/${id}/approve`, {
                method: "PUT",
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        },

        reject: async (id, reason) => {
            const response = await fetch(`${API_BASE_URL}/admin/classes/${id}/reject`, {
                method: "PUT",
                headers: getAuthHeaders(),
                body: JSON.stringify({ reason })
            });
            return handleResponse(response);
        }
    },

    // -------- TEACHERS --------
    Teachers: {
        getAllPending: async () => {
            const response = await fetch(`${API_BASE_URL}/admin/teachers?status=pending`, {
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        },

        approve: async (id) => {
            const response = await fetch(`${API_BASE_URL}/admin/teachers/${id}/approve`, {
                method: "PUT",
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        },

        reject: async (id, reason) => {
            const response = await fetch(`${API_BASE_URL}/admin/teachers/${id}/reject`, {
                method: "PUT",
                headers: getAuthHeaders(),
                body: JSON.stringify({ reason })
            });
            return handleResponse(response);
        }
    },

    // -------- FEES --------
    Fees: {
        getAll: async () => {
            const response = await fetch(`${API_BASE_URL}/admin/fees`, {
                headers: getAuthHeaders()
            });
            return handleResponse(response);
        }
    }
};


// ==================== EXPORT TO WINDOW ====================
window.TMS_API = TMS_API;

console.log("✅ admin.js loaded (clean version – no duplicates)");
