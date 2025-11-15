// ========================================
// PAGE: DASHBOARD ADMIN 
// ========================================

(function () {

    // ---------- Helpers ----------
    function el(id) { return document.getElementById(id); }

    function escapeHtml(s) {
        if (s == null) return "";
        return String(s)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatCurrency(v) {
        try {
            return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v);
        } catch (_) { return v + " ₫"; }
    }

    function subjectLabel(s) {
        if (!s) return "-";
        const map = {
            math: "Toán học",
            physics: "Vật lý",
            chemistry: "Hóa học",
            english: "Tiếng Anh",
            biology: "Sinh học",
            literature: "Ngữ văn",
            history: "Lịch sử",
            geography: "Địa lý"
        };
        return map[String(s).toLowerCase()] || s;
    }

    function getStatusText(s) {
        const map = {
            pending: "Chờ duyệt",
            approved: "Đã duyệt",
            active: "Đang học",
            rejected: "Từ chối",
            completed: "Hoàn thành"
        };
        return map[String(s || "").toLowerCase()] || "Chờ duyệt";
    }

    // ---------- ADMIN INFO ----------
    function loadAdminInfo() {
        try {
            const u = JSON.parse(localStorage.getItem("currentUser") || "{}");
            if (el("adminName")) {
                el("adminName").textContent = `Xin chào, ${u.fullName || u.username || "Admin"}`;
            }
        } catch (e) { }
    }

    // ---------- FETCH CORRECT STATS FROM BACKEND ----------
    async function loadCorrectStatsFromBackend() {
        try {
            const res = await fetch("/api/admin/stats/overview", {
                method: "GET",
                headers: getAuthHeaders()
            });

            if (!res.ok) {
                console.warn("[Dashboard] API stats/overview chưa sẵn sàng, fallback sang API cũ.");
                return null;
            }

            return await res.json();

        } catch (err) {
            console.error("❌ loadCorrectStatsFromBackend error:", err);
            return null;
        }
    }

    // ---------- UPDATE STATS UI ----------
    function updateStatsUI(stats) {
        if (!stats) return;

        if (el("statPendingTeachers"))
            el("statPendingTeachers").textContent = stats.pendingTeachers ?? 0;

        if (el("statActiveClasses"))
            el("statActiveClasses").textContent = stats.activeClasses ?? 0;

        if (el("statTotalStudents"))
            el("statTotalStudents").textContent = stats.totalStudents ?? 0;

        if (el("statRevenue"))
            el("statRevenue").textContent = formatCurrency(stats.revenue ?? stats.monthlyRevenue ?? 0);
    }

    // ---------- RECENT ACTIVITIES ----------
    function updateRecentActivities(list) {
        const box = el("recentActivities");
        if (!box) return;

        if (!list || !list.length) {
            box.innerHTML = `
                <div class="activity-item">
                    <div class="activity-info">
                        <div class="activity-title">Không có hoạt động gần đây</div>
                    </div>
                </div>`;
            return;
        }

        box.innerHTML = list.map(a => `
            <div class="activity-item">
                <div class="activity-info">
                    <div class="activity-title">${escapeHtml(a.className || "Hoạt động")}</div>
                    <div class="activity-time">${escapeHtml(a.subject || "")}</div>
                </div>
                <span class="badge ${a.status === "approved" ? "badge-approved" : "badge-pending"}">
                    ${a.status === "approved" ? "Đã duyệt" : "Chờ duyệt"}
                </span>
            </div>
        `).join("");
    }

    // ---------- PENDING CLASSES TABLE ----------
    function updatePendingClassesTable(raw) {
        const tbody = el("pendingTableBody");
        if (!tbody) return;

        const list = Array.isArray(raw) ? raw : raw?.content || [];
        const pending = list.filter(c => (c.status || "").toLowerCase() === "pending");

        if (!pending.length) {
            tbody.innerHTML = `
                <tr><td colspan="7" style="text-align:center;">Không có lớp chờ duyệt</td></tr>`;
            return;
        }

        tbody.innerHTML = pending.map(c => `
            <tr>
                <td>${escapeHtml(c.className || "-")}</td>
                <td>${escapeHtml(subjectLabel(c.subject))}</td>
                <td>${c.type === "in-school" ? "Trong trường" : "Ngoài trường"}</td>
                <td>${escapeHtml(getStatusText(c.status))}</td>
                <td>${escapeHtml(c.teacher?.fullName || "Chưa phân công")}</td>
                <td>${c.createdAt ? new Date(c.createdAt).toLocaleDateString("vi-VN") : "-"}</td>
                <td>
                    <button class="btn btn-success" onclick="approveClass(${c.id})">Duyệt</button>
                    <button class="btn btn-danger" onclick="rejectClass(${c.id})">Từ chối</button>
                </td>
            </tr>
        `).join("");
    }

    // ---------- LOAD EVERYTHING ----------
    async function loadDashboardData() {
        try {
            // --- Dùng API mới để lấy thống kê thật ---
            const backendStats = await loadCorrectStatsFromBackend();

            if (backendStats) {
                updateStatsUI(backendStats);
            }

            // --- API cũ từ TMS_API giữ nguyên ---
            const [stats, activities, classes] = await Promise.all([
                TMS_API.Dashboard.getStatistics(),
                TMS_API.Dashboard.getRecentActivities(8),
                TMS_API.Classes.getAllAdmin()
            ]);

            updateRecentActivities(activities);
            updatePendingClassesTable(classes);

        } catch (err) {
            console.error("Dashboard load error:", err);
        }
    }

    // ---------- APPROVE / REJECT ----------
    window.approveClass = async function (id) {
        if (!confirm("Duyệt lớp này?")) return;

        try {
            await TMS_API.Classes.approveAdmin(id);
            alert("Đã duyệt!");
            loadDashboardData();

        } catch (e) {
            const body = e.body || {};
            alert("Lỗi duyệt:\n" +
                (body.error || body.message || body.details || e.message));
        }
    };

    window.rejectClass = async function (id) {
        const reason = prompt("Nhập lý do từ chối:");
        if (!reason) return;

        try {
            await TMS_API.Classes.rejectAdmin(id, reason);
            alert("Đã từ chối!");
            loadDashboardData();

        } catch (e) {
            const body = e.body || {};
            alert("Lỗi từ chối:\n" +
                (body.error || body.message || body.details || e.message));
        }
    };

    // ---------- INIT ----------
    document.addEventListener("DOMContentLoaded", async () => {
        if (!requireLoginOrRedirect()) return;
        if (!requireRoleOrRedirect("ADMIN")) return;

        RBAC.applyRoleBasedUI();
        loadAdminInfo();

        await loadDashboardData();

        // Chart giữ nguyên
        if (typeof initChart === "function") initChart();

        // Button tạo lớp từ dashboard
        const btn = el("createClassBtnDashboard");
        if (btn) btn.onclick = () => window.location.href = "manage_classes.html?openCreate=1";
    });

})();
