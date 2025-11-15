// ======================================
// PAGE: MANAGE CLASSES (FINAL MERGED VERSION)
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
    if (s === undefined || s === null) return "";
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
    if (!s) return "Chờ duyệt";
    const map = {
        pending: "Chờ duyệt",
        approved: "Đã duyệt",
        active: "Đang học",
        rejected: "Từ chối",
        completed: "Hoàn thành"
    };
    return map[String(s).toLowerCase()] || String(s);
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
    return map[day] || day || "-";
}

// ====================== STATS ======================
async function loadStats() {
    try {
        const res = await fetch("/api/admin/stats/classes-by-status", {
            method: "GET",
            headers: getAuthHeadersLocal()
        });
        const stats = await res.json();

        document.getElementById("statPending").textContent   = stats?.pending ?? 0;
        document.getElementById("statApproved").textContent  = stats?.approved ?? 0;
        document.getElementById("statActive").textContent    = stats?.active ?? 0;
        document.getElementById("statCompleted").textContent = stats?.completed ?? 0;
    } catch (err) {
        console.error("❌ Lỗi loadStats:", err);
    }
}

// ====================== LOAD CLASSES ======================
async function loadClasses() {
    const tbody = document.getElementById("classesTableBody");
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Đang tải dữ liệu...</td></tr>`;

    try {
        const status = document.getElementById("statusFilter")?.value || "";
        const type   = document.getElementById("typeFilter")?.value || "";
        const subject= document.getElementById("subjectFilter")?.value || "";
        const q      = document.getElementById("searchInput")?.value?.trim() || "";

        const params = new URLSearchParams();
        params.set("page", "0");
        params.set("size", "999");
        if (status) params.set("status", status);
        if (type) params.set("type", type);

        const url = "/api/admin/classes?" + params.toString();
        const res = await fetch(url, { headers: getAuthHeadersLocal() });
        const body = await res.json();

        let list = Array.isArray(body) ? body : (body?.content ?? []);

        if (subject) {
            list = list.filter(c => String(c.subject || "").toLowerCase() === subject.toLowerCase());
        }

        if (q) {
            const qlow = q.toLowerCase();
            list = list.filter(c => {
                const any = [
                    c.className,
                    c.subject,
                    c.teacher?.fullName,
                    c.teacher?.email,
                    c.teacher?.username
                ].filter(Boolean).join(" ").toLowerCase();
                return any.includes(qlow);
            });
        }

        if (!list.length) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Không có lớp học nào</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(c => {
            const teacherName = c.teacher?.fullName || "Chưa phân công";
            return `
                <tr>
                    <td>${escapeHtml(c.className)}</td>
                    <td>${escapeHtml(teacherName)}</td>
                    <td>${escapeHtml(subjectLabel(c.subject))}</td>
                    <td>${c.type === "in-school" ? "Trong trường" : "Ngoài trường"}</td>
                    <td>${c.studentCount ?? (c.students?.length ?? 0)}</td>
                    <td>${Array.isArray(c.schedules) ? c.schedules.length : 0}</td>
                    <td>${getStatusText(c.status)}</td>
                    <td>
                        ${
                            String(c.status).toLowerCase() === "pending"
                            ? `<button class="btn btn-success" onclick="approveClass(${c.id})">Duyệt</button>
                               <button class="btn btn-danger" onclick="rejectClass(${c.id})">Từ chối</button>`
                            : `<button class="btn btn-info" onclick="viewSchedule(${c.id})">Xem lịch</button>`
                        }
                    </td>
                </tr>`;
        }).join("");

    } catch (err) {
        console.error("❌ Load classes error:", err);
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:red">Lỗi tải dữ liệu</td></tr>`;
    }
}

function searchClasses() {
    loadClasses();
}

// ====================== APPROVE CLASS OPEN MODAL ======================
window.approveClass = async function (id) {
    try {
        const res = await fetch(`/api/admin/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        window.currentClassId = id;

        document.getElementById("scheduleClassName").textContent = classData.className || "-";
        document.getElementById("scheduleSubject").textContent   = subjectLabel(classData.subject);
        document.getElementById("scheduleTeacher").textContent   = classData.teacher?.fullName || "Chưa phân công";

        await loadSchedulesForClass(id);
        openScheduleModal();

    } catch (err) {
        console.error("❌ approveClass error:", err);
        alert("Không thể mở modal duyệt lớp!");
    }
};

// ====================== VIEW SCHEDULE ======================
window.viewSchedule = async function (id) {
    try {
        const res = await fetch(`/api/admin/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        document.getElementById("viewClassName").textContent = classData.className || "-";
        document.getElementById("viewClassSubject").textContent = subjectLabel(classData.subject);
        document.getElementById("viewClassTeacher").textContent = classData.teacher?.fullName || "Chưa phân công";

        const box = document.getElementById("viewSchedules");
        const schedules = Array.isArray(classData.schedules) ? classData.schedules : [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học</div>`;
        } else {
            box.innerHTML = schedules.map(s => `
                <div class="schedule-item">
                    <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                    <div class="schedule-time">${escapeHtml(s.startTime)} - ${escapeHtml(s.endTime)}</div>
                </div>
            `).join("");
        }

        document.getElementById("viewScheduleModal").style.display = "flex";

    } catch (err) {
        console.error("❌ viewSchedule error:", err);
        alert("Không thể tải lịch học!");
    }
};

// ====================== LOAD SCHEDULES FOR CLASS ======================
async function loadSchedulesForClass(classId) {
    const box = document.getElementById("addedSchedules");
    box.innerHTML = `<div class="empty-state">Đang tải...</div>`;

    try {
        const res = await fetch(`/api/admin/classes/${classId}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        const schedules = Array.isArray(classData.schedules) ? classData.schedules : [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học nào được thêm</div>`;
            document.getElementById("saveAllBtn").disabled = false;
            return;
        }

        box.innerHTML = schedules.map(s => `
            <div class="schedule-item">
                <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                <div class="schedule-time">${escapeHtml(s.startTime)} - ${escapeHtml(s.endTime)}</div>
            </div>
        `).join("");

        document.getElementById("saveAllBtn").disabled = false;

    } catch (err) {
        console.error("❌ loadSchedules error:", err);
        box.innerHTML = `<div class="empty-state" style="color:red">Lỗi tải dữ liệu</div>`;
    }
}

// ====================== REJECT CLASS ======================
window.rejectClass = async function (id) {
    const reason = prompt("Nhập lý do từ chối:");
    if (reason === null) return;
    try {
        await TMS_API.Classes.rejectAdmin(id, reason);
        alert("Đã từ chối!");
        await loadStats();
        await loadClasses();
    } catch (err) {
        console.error("❌ rejectClass error:", err);
        alert("Lỗi từ chối!");
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

            await loadStats();
            await loadClasses();

            const obj = created.data ?? created;
            window.currentClassId = obj.id;

            document.getElementById("scheduleClassName").textContent = obj.className;
            document.getElementById("scheduleSubject").textContent = subjectLabel(obj.subject);
            document.getElementById("scheduleTeacher").textContent = "Chưa phân công";

            await loadSchedulesForClass(obj.id);
            openScheduleModal();

        } catch (err) {
            console.error("❌ create class error:", err);
            alert("Lỗi tạo lớp!");
        }
    });
}

// ====================== CREATE SCHEDULE ======================
const createScheduleForm = document.getElementById("createScheduleForm");
if (createScheduleForm) {
    createScheduleForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!window.currentClassId) {
            alert("Không có classId");
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

            if (!res.ok) throw new Error(await res.text());

            await loadSchedulesForClass(window.currentClassId);
        } catch (err) {
            console.error("❌ create schedule error:", err);
            alert("Lỗi tạo lịch học!");
        }
    });
}

// ====================== APPROVE CLASS ======================
window.saveAllSchedules = async function () {
    try {
        await TMS_API.Classes.approveAdmin(window.currentClassId);
        alert("Lớp đã được duyệt!");
        closeModal("createScheduleModal");

        await loadStats();
        await loadClasses();

    } catch (err) {
        console.error("❌ saveAllSchedules error:", err);
        alert("Không thể duyệt lớp!");
    }
};

// ============= MODAL FUNCTIONS =============
function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = "none";
}

window.openCreateClassModal = function () {
    const modal = document.getElementById("createClassModal");
    if (modal) modal.style.display = "flex";
};

window.openScheduleModal = function () {
    const modal = document.getElementById("createScheduleModal");
    if (modal) modal.style.display = "flex";
};
