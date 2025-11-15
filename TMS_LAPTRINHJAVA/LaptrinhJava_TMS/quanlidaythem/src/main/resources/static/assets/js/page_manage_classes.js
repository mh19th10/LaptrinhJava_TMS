// page_manage_classes.js
// ======================================
// PAGE: MANAGE CLASSES (KEEP OLD + FIXES)
// ======================================

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireLoginOrRedirect()) return;
    if (!requireRoleOrRedirect("ADMIN")) return;
    RBAC.applyRoleBasedUI();

    await loadStats();
    await loadClasses(); // initial load

    // wire search enter
    document.getElementById("searchInput").addEventListener("keydown", (e) => {
        if (e.key === "Enter") searchClasses();
    });

    // wire filters to reload
    document.getElementById("statusFilter").addEventListener("change", loadClasses);
    document.getElementById("typeFilter").addEventListener("change", loadClasses);
    document.getElementById("subjectFilter").addEventListener("change", loadClasses);
});

// ====================== HELPERS ======================
function getAuthHeadersLocal() {
    // tms-api.js exports getAuthHeaders in global scope; fallback to manual
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
        // backend may not provide completed; keep safe
        document.getElementById("statCompleted").textContent = stats?.completed ?? 0;
    } catch (err) {
        console.error("❌ Lỗi loadStats:", err);
        // leave UI as-is
    }
}

// ====================== LOAD CLASSES ======================
// This function reads filters from DOM and calls backend with query params when possible.
// If TMS_API.Classes.getAllAdmin exists, it is used (it returns pageable object with .content)
async function loadClasses() {
    const tbody = document.getElementById("classesTableBody");
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Đang tải dữ liệu...</td></tr>`;

    try {
        // collect filters
        const status = document.getElementById("statusFilter")?.value || "";
        const type   = document.getElementById("typeFilter")?.value || "";
        const subject= document.getElementById("subjectFilter")?.value || "";
        const q      = document.getElementById("searchInput")?.value?.trim() || "";

        // Prefer backend filtered call if available:
        let payload;
        // If TMS_API.Classes.getAllAdmin accepts only page/size we fallback to direct fetch with params
        // Use direct fetch to /api/admin/classes so we can pass status/type
        const params = new URLSearchParams();
        params.set("page", "0");
        params.set("size", "999");
        if (status) params.set("status", status);
        if (type) params.set("type", type);

        const url = "/api/admin/classes?" + params.toString();
        const res = await fetch(url, { headers: getAuthHeadersLocal() });
        const body = await res.json();

        // body might be Page<ClassDTO> => content array OR plain array
        let list = Array.isArray(body) ? body : (Array.isArray(body?.content) ? body.content : []);
        // map keys: backend returns camelCase already (via DTOMapper), so ok

        // client-side subject filter: backend uses subject param but if not provided apply client filter
        if (subject) {
            list = list.filter(c => {
                // subject from backend might be 'math' etc
                return String(c.subject || "").toLowerCase() === subject.toLowerCase();
            });
        }

        // client-side search q
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

        // update stats counts if needed (optional, but keep top stats in sync)
        // we can compute quick summary client-side
        const counts = { pending:0, approved:0, active:0, completed:0, rejected:0 };
        list.forEach(c => {
            const s = String(c.status || "pending").toLowerCase();
            if (counts[s] !== undefined) counts[s]++; else { /* ignore */ }
        });
        // If you want to override stat cards with these counts, uncomment:
        // document.getElementById("statPending").textContent = counts.pending;
        // document.getElementById("statApproved").textContent = counts.approved;
        // document.getElementById("statActive").textContent = counts.active;
        // document.getElementById("statCompleted").textContent = counts.completed;

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center">Không có lớp học nào</td></tr>`;
            return;
        }

        // build rows
        tbody.innerHTML = list.map(c => {
            const teacherName = c.teacher?.fullName || "Chưa phân công";
            const scheduleCount = Array.isArray(c.schedules) ? c.schedules.length : (c.schedulesCount ?? 0);
            const studentCount = (c.studentCount ?? (Array.isArray(c.students) ? c.students.length : 0));
            const statusText = getStatusText(c.status);
            const safeName = escapeHtml(c.className);
            const safeTeacher = escapeHtml(teacherName);
            const safeSubject = escapeHtml(subjectLabel(c.subject));
            const actions = (String(c.status || "pending").toLowerCase() === "pending")
                ? `<button class="btn btn-success" onclick="approveClass(${c.id})">Duyệt</button>
                   <button class="btn btn-danger" onclick="rejectClass(${c.id})">Từ chối</button>`
                : `<button class="btn btn-info" onclick="viewSchedule(${c.id})">Xem lịch</button>`;

            return `<tr>
                <td>${safeName}</td>
                <td>${safeTeacher}</td>
                <td>${safeSubject}</td>
                <td>${c.type === "in-school" ? "Trong trường" : "Ngoài trường"}</td>
                <td>${studentCount}</td>
                <td>${scheduleCount}</td>
                <td>${statusText}</td>
                <td>${actions}</td>
            </tr>`;
        }).join("");

    } catch (err) {
        console.error("❌ Load classes error:", err);
        document.getElementById("classesTableBody").innerHTML =
            `<tr><td colspan="8" style="text-align:center;color:red">Lỗi tải dữ liệu</td></tr>`;
    }
}

// helper wrapper for filters (keeps same name as html onchange)
function loadClassesData() {
    loadClasses();
}

// ====================== SEARCH ======================
function searchClasses() {
    loadClasses();
}

// ====================== APPROVE (OPEN SCHEDULE MODAL) ======================
window.approveClass = async function (id) {
    try {
        // Get class detail (admin endpoint)
        const res = await fetch(`/api/admin/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        // Fill schedule modal (createScheduleModal) with class info
        window.currentClassId = id;
        document.getElementById("scheduleClassName").textContent = classData.className || "-";
        document.getElementById("scheduleSubject").textContent = subjectLabel(classData.subject);
        document.getElementById("scheduleTeacher").textContent = classData.teacher?.fullName || "Chưa phân công";

        // load schedules for this class
        await loadSchedulesForClass(id);

        openScheduleModal();
    } catch (err) {
        console.error("❌ approveClass error:", err);
        alert("Không thể mở modal duyệt lớp: " + (err.message || err));
    }
};

// ====================== VIEW SCHEDULE ======================
window.viewSchedule = async function (id) {
    try {
        window.currentClassId = id;
        const res = await fetch(`/api/admin/classes/${id}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();

        document.getElementById("viewClassName").textContent = classData.className || "-";
        document.getElementById("viewClassSubject").textContent = subjectLabel(classData.subject);
        document.getElementById("viewClassTeacher").textContent = classData.teacher?.fullName || "Chưa phân công";

        // populate viewSchedules area
        const box = document.getElementById("viewSchedules");
        box.innerHTML = `<div class="empty-state">Đang tải...</div>`;

        // schedules are returned inside classData.schedules
        const schedules = Array.isArray(classData.schedules) ? classData.schedules : [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học</div>`;
        } else {
            box.innerHTML = schedules.map(s => `
                <div class="schedule-item">
                    <div class="schedule-info">
                        <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                        <div class="schedule-time">${escapeHtml(s.startTime)} - ${escapeHtml(s.endTime)}</div>
                    </div>
                </div>
            `).join("");
        }

        // open modal
        document.getElementById("viewScheduleModal").style.display = "flex";
    } catch (err) {
        console.error("❌ viewSchedule error:", err);
        alert("Không thể tải lịch học: " + (err.message || err));
    }
};

// ====================== LOAD SCHEDULES FOR MODAL (createScheduleModal) ======================
async function loadSchedulesForClass(classId) {
    const box = document.getElementById("addedSchedules");
    box.innerHTML = `<div class="empty-state">Đang tải...</div>`;

    try {
        // Use admin class endpoint which contains schedules array
        const res = await fetch(`/api/admin/classes/${classId}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.json();
        const schedules = Array.isArray(classData.schedules) ? classData.schedules : [];

        if (!schedules.length) {
            box.innerHTML = `<div class="empty-state">Chưa có lịch học nào được thêm</div>`;
            document.getElementById("saveAllBtn").disabled = false; // allow saving (approve) even with 0? keep enabled per your flow; you can toggle
            return;
        }

        box.innerHTML = schedules.map(s => `
            <div class="schedule-item">
                <div class="schedule-info">
                    <div class="schedule-day">${vietnameseDay(s.dayOfWeek)}</div>
                    <div class="schedule-time">${escapeHtml(s.startTime)} - ${escapeHtml(s.endTime)}</div>
                </div>
            </div>
        `).join("");
        document.getElementById("saveAllBtn").disabled = false;
    } catch (e) {
        console.error("Lỗi tải schedules:", e);
        box.innerHTML = `<div class="empty-state" style="color:red">Lỗi tải dữ liệu</div>`;
    }
}

// ====================== REJECT ======================
window.rejectClass = async function (id) {
    const reason = prompt("Nhập lý do từ chối:");
    if (reason === null) return; // user cancelled (allow empty string? your backend saves "Không có lý do" if blank)
    try {
        await TMS_API.Classes.rejectAdmin(id, reason);
        alert("Đã từ chối!");
        await loadStats();
        await loadClasses();
    } catch (err) {
        console.error("❌ rejectClass error:", err);
        alert("Lỗi từ chối: " + (err.body?.message || err.message || err));
    }
};

// ====================== CREATE CLASS (FORM) ======================
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

            // refresh
            await loadStats();
            await loadClasses();

            // open schedule modal for newly created
            if (created?.data) {
                // TMS_API may return wrapped response { success, data } or direct object
                window.currentClassId = created.data.id ?? created.id;
            } else {
                window.currentClassId = created.id;
            }

            // prefill schedule modal
            document.getElementById("scheduleClassName").textContent = created.className ?? (created.data?.className ?? "-");
            document.getElementById("scheduleSubject").textContent = subjectLabel(created.subject ?? created.data?.subject);
            document.getElementById("scheduleTeacher").textContent = "Chưa phân công";

            await loadSchedulesForClass(window.currentClassId);
            openScheduleModal();

        } catch (err) {
            console.error("❌ create class error:", err);
            alert("Lỗi tạo lớp: " + (err.body?.message || err.message || err));
        }
    });
}

// ====================== ADD NEW SCHEDULE (FORM) ======================
const createScheduleForm = document.getElementById("createScheduleForm");
if (createScheduleForm) {
    createScheduleForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (!window.currentClassId) {
            alert("Không có class id hiện tại.");
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
            if (!res.ok) {
                const txt = await res.text();
                throw new Error(txt || "Lỗi tạo lịch");
            }
            await loadSchedulesForClass(window.currentClassId);
        } catch (err) {
            console.error("❌ create schedule error:", err);
            alert("Lỗi tạo lịch học: " + (err.message || err));
        }
    });
}

// ====================== SAVE ALL SCHEDULES (APPROVE CLASS) ======================
window.saveAllSchedules = async function () {
    try {
        if (!window.currentClassId) {
            alert("Không có lớp để duyệt.");
            return;
        }
        await TMS_API.Classes.approveAdmin(window.currentClassId);
        alert("🎉 Lớp đã được duyệt!");
        closeModal("createScheduleModal");
        await loadStats();
        await loadClasses();
    } catch (err) {
        console.error("❌ saveAllSchedules error:", err);
        alert("Không thể duyệt lớp: " + (err.body?.message || err.message || err));
    }
};

// small utility closeModal used in HTML
function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = "none";
}
