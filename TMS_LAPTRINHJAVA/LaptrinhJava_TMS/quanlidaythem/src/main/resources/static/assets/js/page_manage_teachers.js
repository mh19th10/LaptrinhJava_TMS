// ======================================
// PAGE: MANAGE TEACHERS
// ======================================

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireLoginOrRedirect()) return;
    if (!requireRoleOrRedirect("ADMIN")) return;
    RBAC.applyRoleBasedUI();
    await loadTeachers();
});

// ====================== LOAD TEACHERS ======================

async function loadTeachers() {
    const pendingBody  = document.getElementById("pendingTableBody");
    const approvedBody = document.getElementById("approvedTableBody");
    const rejectedBody = document.getElementById("rejectedTableBody");

    // show loading
    pendingBody.innerHTML  = `<tr><td colspan="6">Đang tải...</td></tr>`;
    approvedBody.innerHTML = `<tr><td colspan="6">Đang tải...</td></tr>`;
    rejectedBody.innerHTML = `<tr><td colspan="6">Đang tải...</td></tr>`;

    try {
        const teachers = await TMS_API.Teachers.getAll();
        console.log("Teachers:", teachers);

        // backend trả status = APPROVED / PENDING / REJECTED
        let pendingList  = teachers.filter(t => t.status?.toUpperCase() === "PENDING");
        let approvedList = teachers.filter(t => t.status?.toUpperCase() === "APPROVED");
        let rejectedList = teachers.filter(t => t.status?.toUpperCase() === "REJECTED");

        document.getElementById("pendingCount").textContent  = pendingList.length;
        document.getElementById("approvedCount").textContent = approvedList.length;
        document.getElementById("rejectedCount").textContent = rejectedList.length;

        // ====================== RENDER PENDING ======================
        pendingBody.innerHTML = pendingList.length ? pendingList.map(t => `
            <tr>
                <td>${(t.fullName || t.name || "-")}</td>
                <td>${t.email ?? '-'}</td>
                <td>${formatSubjects(t.subjects)}</td>
                <td>-</td>
                <td><span class="badge badge-pending">Chờ duyệt</span></td>
                <td>
                    <button class="btn btn-success" onclick="approveTeacher(${t.id})">Duyệt</button>
                    <button class="btn btn-danger" onclick="rejectTeacher(${t.id})">Từ chối</button>
                </td>
            </tr>
        `).join("") : `<tr><td colspan="6" style="text-align:center;">Không có giáo viên</td></tr>`;

        // ====================== RENDER APPROVED ======================
        approvedBody.innerHTML = approvedList.length ? approvedList.map(t => `
            <tr>
                <td>${(t.fullName || t.name || "-")}</td>
                <td>${t.email ?? '-'}</td>
                <td>${formatSubjects(t.subjects)}</td>
                <td>${t.classCount ?? 0}</td>
                <td><span class="badge badge-approved">Đã duyệt</span></td>
                <td>
                    <button class="btn btn-danger" onclick="revokeTeacher(${t.id})">Hủy quyền</button>
                </td>
            </tr>
        `).join("") : `<tr><td colspan="6" style="text-align:center;">Không có giáo viên</td></tr>`;

        // ====================== RENDER REJECTED ======================
        rejectedBody.innerHTML = rejectedList.length ? rejectedList.map(t => `
            <tr>
                <td>${(t.fullName || t.name || "-")}</td>
                <td>${t.email ?? '-'}</td>
                <td>${formatSubjects(t.subjects)}</td>
                <td>${t.rejectReason ?? "Không có"}</td>
                <td><span class="badge badge-rejected">Từ chối</span></td>
                <td>-</td>
            </tr>
        `).join("") : `<tr><td colspan="6" style="text-align:center;">Không có giáo viên</td></tr>`;

    } catch (err) {
        console.error("❌ Load teachers error:", err);
        pendingBody.innerHTML = `<tr><td colspan="6">Lỗi tải dữ liệu</td></tr>`;
        approvedBody.innerHTML = `<tr><td colspan="6">Lỗi tải dữ liệu</td></tr>`;
        rejectedBody.innerHTML = `<tr><td colspan="6">Lỗi tải dữ liệu</td></tr>`;
    }
}

// ====================== ACTIONS ======================

async function approveTeacher(id) {
    if (!confirm("Duyệt giáo viên này?")) return;
    await TMS_API.Teachers.approve(id);
    loadTeachers();
}

async function rejectTeacher(id) {
    const reason = prompt("Lý do từ chối:");
    if (!reason) return;
    await TMS_API.Teachers.reject(id, reason);
    loadTeachers();
}

async function revokeTeacher(id) {
    if (!confirm("Bạn có chắc muốn hủy quyền đã được cấp cho giáo viên này? Giáo viên có thể đăng ký lại sau nếu muốn.")) return;
    try {
        await TMS_API.Teachers.revoke(id);
        alert("Đã hủy quyền thành công.");
        loadTeachers();
    } catch (err) {
        alert("Lỗi: " + (err.message || "Không thể hủy quyền."));
    }
}

// ====================== HELPERS ======================

function subjectLabel(key) {
    const map = {
        math: "Toán học", physics: "Vật lý", chemistry: "Hóa học",
        english: "Tiếng Anh", biology: "Sinh học",
        literature: "Ngữ văn", history: "Lịch sử", geography: "Địa lý"
    };
    return map[key] || key;
}

// Hiển thị danh sách các môn đã đăng ký
function formatSubjects(subjects) {
    if (!subjects || !Array.isArray(subjects) || subjects.length === 0) {
        return "-";
    }
    // Hiển thị danh sách môn với trạng thái
    return subjects.map(s => {
        const status = s.active ? "✓" : "⏳";
        const name = s.name || s.code || `Môn ${s.subjectId}`;
        return `${status} ${name}`;
    }).join(", ");
}

function switchTab(tab) {
    document.querySelectorAll(".tab").forEach(btn => btn.classList.remove("active"));
    const idx = { pending: 0, approved: 1, rejected: 2 }[tab];
    document.querySelectorAll(".tab")[idx].classList.add("active");

    document.getElementById("pendingTab").style.display  = (tab === "pending")  ? "" : "none";
    document.getElementById("approvedTab").style.display = (tab === "approved") ? "" : "none";
    document.getElementById("rejectedTab").style.display = (tab === "rejected") ? "" : "none";
}
