// page_manage_classes.js
// ======================================
// PAGE: MANAGE CLASSES (FIXED VERSION)
// ======================================

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireLoginOrRedirect()) return;
    if (!requireRoleOrRedirect("ADMIN")) return;
    RBAC.applyRoleBasedUI();

    await loadStats();
    await loadClasses();

    document.getElementById("searchInput").addEventListener("keydown", (e) => {
        if (e.key === "Enter") searchClasses();
    });

    document.getElementById("statusFilter").addEventListener("change", loadClasses);
    document.getElementById("typeFilter").addEventListener("change", loadClasses);
    document.getElementById("subjectFilter").addEventListener("change", loadClasses);
});

// ====================== HELPERS ======================
function getAuthHeadersLocal() {
    if (typeof getAuthHeaders === "function") return getAuthHeaders();
    const token = localStorage.getItem("authToken");
    return {
        "Content-Type": "application/json",
        ...(token ? { "Authorization": token.startsWith("Bearer ") ? token : "Bearer " + token } : {})
    };
}

function escapeHtml(s) {
    if (s == null) return "";
    return String(s)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function subjectLabel(s) {
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
    return map[s] || s || "-";
}

function getStatusText(s) {
    const map = {
        pending: "Chờ duyệt",
        approved: "Đã duyệt",
        active: "Đang học",
        rejected: "Từ chối",
        completed: "Hoàn thành"
    };
    return map[(s || "").toLowerCase()] || "Chờ duyệt";
}

function vietnameseDay(day) {
    const map = {
        MONDAY: "Thứ Hai",
        TUESDAY: "Thứ Ba",
        WEDNESDAY: "Thứ Tư",
        THURSDAY: "Thứ Năm",
        FRIDAY: "Thứ Sáu",
        SATURDAY: "Thứ Bảy",
        SUNDAY: "Chủ Nhật"
    };
    return map[day] || day;
}

// ====================== STATS ======================
async function loadStats() {
    try {
        const res = await fetch("/api/admin/stats/classes-by-status", {
            headers: getAuthHeadersLocal()
        });
        const stats = await res.json();

        document.getElementById("statPending").textContent = stats?.pending ?? 0;
        document.getElementById("statApproved").textContent = stats?.approved ?? 0;
        document.getElementById("statActive").textContent = stats?.active ?? 0;
        document.getElementById("statCompleted").textContent = stats?.completed ?? 0;

    } catch (err) {
        console.error("❌ loadStats error:", err);
    }
}

// ====================== LOAD CLASSES ======================
async function loadClasses() {
    const tbody = document.getElementById("classesTableBody");
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Đang tải dữ liệu...</td></tr>`;

    try {
        const url = "/api/classes";
        const res = await fetch(url, { headers: getAuthHeadersLocal() });
        const body = await res.json();

        let list = Array.isArray(body) ? body : (body?.content ?? []);

        const status = document.getElementById("statusFilter")?.value?.trim();
        const type = document.getElementById("typeFilter")?.value?.trim();
        const subject = document.getElementById("subjectFilter")?.value?.trim();
        const q = document.getElementById("searchInput")?.value?.trim().toLowerCase();

        if (status) list = list.filter(c => c.status?.toLowerCase() === status.toLowerCase());
        if (type) list = list.filter(c => c.type === type);
        if (subject) list = list.filter(c => c.subject?.toLowerCase() === subject.toLowerCase());

        if (q) {
            list = list.filter(c => {
                const text = `${c.className} ${c.subject} ${c.teacher?.fullName}`.toLowerCase();
                return text.includes(q);
            });
        }

        if (!list.length) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Không có lớp học nào</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(c => `
            <tr>
                <td>${escapeHtml(c.className)}</td>
                <td>${escapeHtml(c.teacher?.fullName || "Chưa phân công")}</td>
                <td>${escapeHtml(subjectLabel(c.subject))}</td>
                <td>${c.type === "in-school" ? "Trong trường" : "Ngoài trường"}</td>
                <td>${c.studentCount ?? (c.students?.length ?? 0)}</td>
                <td>${c.schedules?.length ?? 0}</td>
                <td>${getStatusText(c.status)}</td>
                <td>
                    ${
                        c.status === "pending"
                        ? `<button class="btn btn-success" onclick="approveClass(${c.id})">Duyệt</button>
                           <button class="btn btn-danger" onclick="rejectClass(${c.id})">Từ chối</button>`
                        : `<button class="btn btn-info" onclick="viewSchedule(${c.id})">Xem lịch</button>`
                    }
                </td>
            </tr>
        `).join("");

    } catch (err) {
        console.error("❌ loadClasses error:", err);
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:red">Lỗi tải dữ liệu</td></tr>`;
    }
}

function searchClasses() {
    loadClasses();
}

// ====================== APPROVE CLASS ======================
window.approveClass = async function (id) {
    try {
        const res = await fetch(`/api/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        window.currentClassId = id;

        document.getElementById("scheduleClassName").textContent = classData.className;
        document.getElementById("scheduleSubject").textContent = subjectLabel(classData.subject);
        document.getElementById("scheduleTeacher").textContent = classData.teacher?.fullName || "Chưa phân công";

        await loadSchedulesForClass(id);

        openScheduleModal();

    } catch (err) {
        console.error("❌ approveClass error:", err);
        alert("Không thể mở modal duyệt lớp");
    }
};

// ====================== VIEW SCHEDULE ======================
window.viewSchedule = async function (id) {
    try {
        const res = await fetch(`/api/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        window.currentClassId = id;

        document.getElementById("viewClassName").textContent = classData.className;
        document.getElementById("viewClassSubject").textContent = subjectLabel(classData.subject);
        document.getElementById("viewClassTeacher").textContent = classData.teacher?.fullName || "Chưa phân công";

        const box = document.getElementById("viewSchedules");
        const schedules = classData.schedules ?? [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học</div>`;
        } else {
            box.innerHTML = schedules.map(s => `
                <div class="schedule-item">
                    <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                    <div class="schedule-time">${s.startTime} - ${s.endTime}</div>
                </div>
            `).join("");
        }

        document.getElementById("viewScheduleModal").style.display = "flex";

    } catch (err) {
        console.error("❌ viewSchedule error:", err);
        alert("Lỗi tải lịch");
    }
};

// ====================== LOAD SCHEDULES FOR MODAL ======================
async function loadSchedulesForClass(classId) {
    const box = document.getElementById("addedSchedules");
    box.innerHTML = `<div class="empty-state">Đang tải...</div>`;

    try {
        const res = await fetch(`/api/classes/${classId}`, {
            headers: getAuthHeadersLocal()
        });

        const classData = await res.json();
        const schedules = classData.schedules ?? [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học nào</div>`;
            return;
        }

        box.innerHTML = schedules.map(s => `
            <div class="schedule-item">
                <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                <div class="schedule-time">${s.startTime} - ${s.endTime}</div>
            </div>
        `).join("");

        // 🔥 Bật nút lưu lịch học
        document.getElementById("saveAllBtn").disabled = false;

    } catch (err) {
        console.error("❌ loadSchedules error:", err);
    }
}


// ====================== REJECT CLASS ======================
window.rejectClass = async function (id) {
    const reason = prompt("Nhập lý do từ chối:");
    if (reason == null) return;

    try {
        await TMS_API.Classes.rejectAdmin(id, reason);
        alert("Đã từ chối!");
        loadClasses();
    } catch (err) {
        alert("Lỗi từ chối");
    }
};

// ====================== CREATE CLASS ======================
const createClassForm = document.getElementById("createClassForm");
if (createClassForm) {
    createClassForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            className: document.getElementById("className").value,
            subject: document.getElementById("classSubject").value,
            type: document.querySelector("input[name='type']:checked")?.value,
            description: document.getElementById("classDescription").value
        };

        try {
            const created = await TMS_API.Classes.create(payload);
            alert("Tạo lớp thành công!");
            closeModal("createClassModal");

            await loadClasses();

            const classObj = created.data ?? created; // Tự động lấy đúng format

            window.currentClassId = classObj.id;

            document.getElementById("scheduleClassName").textContent = classObj.className;
            document.getElementById("scheduleSubject").textContent = subjectLabel(classObj.subject);
            document.getElementById("scheduleTeacher").textContent = classObj.teacher?.fullName || "Chưa phân công";


            await loadSchedulesForClass(window.currentClassId);
            openScheduleModal();

        } catch (err) {
            console.error("❌ create class error:", err);
            alert("Lỗi tạo lớp");
        }
    });
}

// ====================== CREATE SCHEDULE ======================
const createScheduleForm = document.getElementById("createScheduleForm");
if (createScheduleForm) {
    createScheduleForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!window.currentClassId) {
            alert("Lỗi: không có classId");
            return;
        }

        const payload = {
            dayOfWeek: document.getElementById("dayOfWeek").value,
            startTime: document.getElementById("startTime").value,
            endTime: document.getElementById("endTime").value
        };

        try {
            const res = await fetch(`/api/classes/${window.currentClassId}/schedule`, {
                method: "POST",
                headers: getAuthHeadersLocal(),
                body: JSON.stringify(payload)
            });

            if (!res.ok) throw new Error("Lỗi tạo lịch");

            await loadSchedulesForClass(window.currentClassId);

        } catch (err) {
            console.error("❌ create schedule error:", err);
            alert("Lỗi tạo lịch");
        }
    });
}

// ====================== APPROVE CLASS FINAL ======================
window.saveAllSchedules = async function () {
    try {
        await TMS_API.Classes.approveAdmin(window.currentClassId);
        alert("Lớp đã được duyệt!");
        closeModal("createScheduleModal");
        loadClasses();
    } catch (err) {
        alert("Lỗi duyệt lớp");
    }
};

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = "none";
}

window.openCreateClassModal = function () {
    const modal = document.getElementById("createClassModal");
    if (modal) modal.style.display = "flex";
};

window.openScheduleModal = function() {
    const modal = document.getElementById("createScheduleModal");
    if (modal) modal.style.display = "flex";
};

