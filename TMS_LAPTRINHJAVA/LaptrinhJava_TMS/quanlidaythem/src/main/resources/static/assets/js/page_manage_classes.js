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

// Cache để lưu subject names và teacher names
let subjectNamesCache = {};
let teacherNamesCache = {};

async function getSubjectNameById(subjectId) {
    if (!subjectId) return "-";
    if (subjectNamesCache[subjectId]) return subjectNamesCache[subjectId];
    
    try {
        const res = await fetch(`/api/subjects/${subjectId}`, {
            headers: getAuthHeadersLocal()
        });
        if (res.ok) {
            const subject = await res.json();
            const name = subject.name || subject.subjectName || "-";
            subjectNamesCache[subjectId] = name;
            return name;
        }
    } catch (err) {
        console.error("Error loading subject:", err);
    }
    
    // Fallback: load all subjects
    try {
        const res = await fetch("/api/subjects", {
            headers: getAuthHeadersLocal()
        });
        if (res.ok) {
            const subjects = await res.json();
            const arr = Array.isArray(subjects) ? subjects : (subjects?.content || []);
            arr.forEach(s => {
                if (s.id) subjectNamesCache[s.id] = s.name || s.subjectName || "-";
            });
            return subjectNamesCache[subjectId] || "-";
        }
    } catch (err) {
        console.error("Error loading subjects:", err);
    }
    
    return "-";
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

        // Try both endpoints: /api/admin/classes and /api/admin/class-management
        let url = "/api/admin/class-management?" + params.toString();
        let res = await fetch(url, { headers: getAuthHeadersLocal() });
        
        // If class-management endpoint fails, try classes endpoint
        if (!res.ok) {
            url = "/api/admin/classes?" + params.toString();
            res = await fetch(url, { headers: getAuthHeadersLocal() });
        }
        
        const body = await res.json();
        let list = Array.isArray(body) ? body : (body?.content ?? []);
        
        // Nếu status là "pending" hoặc không có status filter, cũng load pending registrations
        if (status === "pending" || status === "") {
            try {
                // Load subjects và teachers trước để cache
                try {
                    const [subjRes, teacherRes] = await Promise.all([
                        fetch("/api/subjects", { headers: getAuthHeadersLocal() }),
                        fetch("/api/admin/teachers", { headers: getAuthHeadersLocal() })
                    ]);
                    
                    if (subjRes.ok) {
                        const subjects = await subjRes.json();
                        const arr = Array.isArray(subjects) ? subjects : (subjects?.content || []);
                        arr.forEach(s => {
                            if (s.id) subjectNamesCache[s.id] = s.name || s.subjectName || "-";
                        });
                    }
                    
                    if (teacherRes.ok) {
                        const teachers = await teacherRes.json();
                        const arr = Array.isArray(teachers) ? teachers : (teachers?.content || []);
                        arr.forEach(t => {
                            // TeacherRegistration có teacherId là user.id, không phải teacher.id
                            // Nên cần map qua User entity, nhưng tạm thời dùng fullName nếu có
                            if (t.id) {
                                // Cache theo cả id và fullName
                                teacherNamesCache[t.id] = t.fullName || "-";
                            }
                        });
                    }
                } catch (err) {
                    console.error("Error loading subjects/teachers:", err);
                }
                
                const regRes = await fetch("/api/admin/teach/registrations/pending", {
                    headers: getAuthHeadersLocal()
                });
                if (regRes.ok) {
                    const registrations = await regRes.json();
                    // Convert registrations to class-like objects
                    const regList = registrations.map(reg => ({
                        id: "reg_" + reg.id, // Prefix để phân biệt với class id
                        registrationId: reg.id,
                        className: reg.className || "-",
                        subject: reg.subjectId ? (subjectNamesCache[reg.subjectId] || reg.subjectId) : "-",
                        subjectId: reg.subjectId,
                        type: reg.mode || "out-school",
                        capacity: reg.capacity || 30,
                        status: "pending",
                        isRegistration: true, // Flag để biết đây là registration
                        teacherId: reg.teacherId,
                        teacher: reg.teacherId ? { fullName: teacherNamesCache[reg.teacherId] || `Giáo viên #${reg.teacherId}` } : null,
                        location: reg.location || "-",
                        schedule: reg.schedule || "-",
                        requestNote: reg.requestNote || "-",
                        studentCount: 0,
                        schedules: []
                    }));
                    list = [...regList, ...list]; // Thêm registrations vào đầu danh sách
                }
            } catch (err) {
                console.error("❌ Load pending registrations error:", err);
            }
        }

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
            const isReg = c.isRegistration || String(c.id).startsWith("reg_");
            const regId = isReg ? (c.registrationId || String(c.id).replace("reg_", "")) : null;
            
            return `
                <tr>
                    <td>${escapeHtml(c.className)}</td>
                    <td>${escapeHtml(teacherName)}</td>
                    <td>${escapeHtml(subjectLabel(c.subject))}</td>
                    <td>${c.type === "in-school" ? "Trong trường" : "Ngoài trường"}</td>
                    <td>${c.studentCount ?? (c.students?.length ?? 0)}</td>
                    <td>${isReg ? "-" : (Array.isArray(c.schedules) ? c.schedules.length : 0)}</td>
                    <td>${getStatusText(c.status)}</td>
                    <td>
                        <div class="btn-group">
                            ${
                                isReg
                                ? `<button class="btn btn-success" onclick="approveRegistration(${regId}, '${escapeHtml(c.className || '')}', ${c.subjectId || 'null'}, ${c.capacity || 'null'})">Duyệt</button>
                                   <button class="btn btn-danger" onclick="rejectRegistration(${regId})">Từ chối</button>`
                                : String(c.status).toLowerCase() === "pending"
                                ? `<button class="btn btn-success" onclick="approveClass(${c.id})">Duyệt</button>
                                   <button class="btn btn-danger" onclick="rejectClass(${c.id})">Từ chối</button>`
                                : `<button class="btn btn-info" onclick="viewSchedule(${c.id})">Xem lịch</button>`
                            }
                            ${!isReg ? `<button class="btn btn-info" onclick="viewStudents(${c.id})">Xem học sinh (${c.studentCount ?? (c.students?.length ?? 0)})</button>` : ''}
                        </div>
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
        // Try both endpoints
        let res = await fetch(`/api/admin/class-management/${id}`, {
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) {
            res = await fetch(`/api/admin/classes/${id}`, {
                headers: getAuthHeadersLocal()
            });
        }
        
        if (!res.ok) throw new Error(await res.text());
        
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
        // Try both endpoints
        let res = await fetch(`/api/admin/class-management/${id}`, {
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) {
            res = await fetch(`/api/admin/classes/${id}`, {
                headers: getAuthHeadersLocal()
            });
        }
        
        if (!res.ok) throw new Error(await res.text());
        
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
        // Try both endpoints
        let res = await fetch(`/api/admin/class-management/${classId}`, {
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) {
            res = await fetch(`/api/admin/classes/${classId}`, {
                headers: getAuthHeadersLocal()
            });
        }
        
        if (!res.ok) throw new Error(await res.text());
        
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

// ====================== APPROVE REGISTRATION ======================
window.approveRegistration = async function (regId, className, subjectId, capacity) {
    try {
        const token = localStorage.getItem("authToken");
        const userStr = localStorage.getItem("user");
        const user = userStr ? JSON.parse(userStr) : null;
        const adminId = user?.id || 1; // Fallback to 1 if no user
        
        const body = {
            className: className || null,
            subjectId: subjectId ? Number(subjectId) : null,
            capacity: capacity ? Number(capacity) : null
        };
        
        const res = await fetch(`/api/admin/teach/registrations/${regId}/approve`, {
            method: "POST",
            headers: {
                ...getAuthHeadersLocal(),
                "X-Admin-Id": String(adminId)
            },
            body: JSON.stringify(body)
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        alert("Đã duyệt đăng ký và tạo lớp mới!");
        await loadStats();
        await loadClasses();
    } catch (err) {
        console.error("❌ approveRegistration error:", err);
        alert("Lỗi duyệt đăng ký: " + (err.message || "Không thể duyệt"));
    }
};

// ====================== REJECT REGISTRATION ======================
window.rejectRegistration = async function (regId) {
    const reason = prompt("Nhập lý do từ chối:");
    if (reason === null) return;
    
    try {
        const token = localStorage.getItem("authToken");
        const userStr = localStorage.getItem("user");
        const user = userStr ? JSON.parse(userStr) : null;
        const adminId = user?.id || 1; // Fallback to 1 if no user
        
        const res = await fetch(`/api/admin/teach/registrations/${regId}/reject`, {
            method: "POST",
            headers: {
                ...getAuthHeadersLocal(),
                "X-Admin-Id": String(adminId)
            },
            body: JSON.stringify({ reason })
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        alert("Đã từ chối đăng ký!");
        await loadStats();
        await loadClasses();
    } catch (err) {
        console.error("❌ rejectRegistration error:", err);
        alert("Lỗi từ chối đăng ký: " + (err.message || "Không thể từ chối"));
    }
};

// ====================== REJECT CLASS ======================
window.rejectClass = async function (id) {
    const reason = prompt("Nhập lý do từ chối:");
    if (reason === null) return;
        try {
            // Use class-management endpoint
            const res = await fetch(`/api/admin/class-management/${id}/reject`, {
                method: "POST",
                headers: getAuthHeadersLocal(),
                body: JSON.stringify({ reason })
            });
            
            if (!res.ok) throw new Error(await res.text());
            
            alert("Đã từ chối!");
            await loadStats();
            await loadClasses();
        } catch (err) {
            console.error("❌ rejectClass error:", err);
            alert("Lỗi từ chối: " + (err.message || "Không thể từ chối lớp"));
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
        // Use class-management endpoint
        const res = await fetch(`/api/admin/class-management/${window.currentClassId}/approve`, {
            method: "POST",
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        alert("Lớp đã được duyệt!");
        closeModal("createScheduleModal");

        await loadStats();
        await loadClasses();

    } catch (err) {
        console.error("❌ saveAllSchedules error:", err);
        alert("Không thể duyệt lớp: " + (err.message || "Lỗi không xác định"));
    }
};

// ====================== VIEW STUDENTS =====================
window.viewStudents = async function (classId) {
    try {
        const res = await fetch(`/api/admin/class-management/${classId}/students`, {
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        const students = await res.json();
        
        // Load class info
        const classRes = await fetch(`/api/admin/class-management/${classId}`, {
            headers: getAuthHeadersLocal()
        });
        const classData = await res.ok ? await classRes.json() : null;
        
        document.getElementById("viewStudentsClassName").textContent = classData?.className || "Lớp #" + classId;
        document.getElementById("viewStudentsClassSubject").textContent = classData ? subjectLabel(classData.subject) : "-";
        document.getElementById("viewStudentsClassType").textContent = classData?.type === "in-school" ? "Trong trường" : "Ngoài trường";
        
        const studentsBox = document.getElementById("viewStudentsList");
        
        if (!students || students.length === 0) {
            studentsBox.innerHTML = '<div class="empty-state">Chưa có học sinh nào đăng ký</div>';
        } else {
            studentsBox.innerHTML = students.map(s => `
                <div class="schedule-item">
                    <div class="schedule-info">
                        <div class="schedule-day">${escapeHtml(s.fullName || '-')}</div>
                        <div class="schedule-time">Email: ${escapeHtml(s.email || '-')} | SĐT: ${escapeHtml(s.phone || '-')}</div>
                    </div>
                    <button class="btn btn-danger" onclick="removeStudentFromClass(${classId}, ${s.id})" style="margin-left: 10px;">Xóa</button>
                </div>
            `).join("");
        }
        
        window.currentViewStudentsClassId = classId;
        document.getElementById("viewStudentsModal").style.display = "flex";
        
    } catch (err) {
        console.error("❌ viewStudents error:", err);
        alert("Không thể tải danh sách học sinh!");
    }
};

// ====================== REMOVE STUDENT FROM CLASS =====================
window.removeStudentFromClass = async function (classId, studentId) {
    if (!confirm("Bạn có chắc muốn xóa học sinh này khỏi lớp?")) return;
    
    try {
        const res = await fetch(`/api/admin/class-management/${classId}/students/${studentId}`, {
            method: "DELETE",
            headers: getAuthHeadersLocal()
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        alert("Đã xóa học sinh khỏi lớp!");
        
        // Reload students list
        await viewStudents(classId);
        
        // Reload classes list
        await loadClasses();
        await loadStats();
        
    } catch (err) {
        console.error("❌ removeStudentFromClass error:", err);
        alert("Lỗi xóa học sinh!");
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
