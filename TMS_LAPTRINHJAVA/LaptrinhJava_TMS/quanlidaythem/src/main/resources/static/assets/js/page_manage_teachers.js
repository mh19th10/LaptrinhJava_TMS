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
        console.log("✅ Loaded teachers:", teachers);
        console.log("📊 Total teachers:", teachers?.length || 0);

        // backend trả status = APPROVED / PENDING / REJECTED
        let pendingList  = teachers.filter(t => t.status?.toUpperCase() === "PENDING");
        let approvedList = teachers.filter(t => t.status?.toUpperCase() === "APPROVED");
        let rejectedList = teachers.filter(t => t.status?.toUpperCase() === "REJECTED");
        
        console.log("📋 Pending teachers:", pendingList.length);
        console.log("✅ Approved teachers:", approvedList.length);
        console.log("❌ Rejected teachers:", rejectedList.length);

        document.getElementById("pendingCount").textContent  = pendingList.length;
        document.getElementById("approvedCount").textContent = approvedList.length;
        document.getElementById("rejectedCount").textContent = rejectedList.length;

        // ====================== RENDER PENDING ======================
        // Load subjects để hiển thị tên môn học
        let subjectNamesCache = {};
        try {
            const subjectsRes = await fetch("/api/subjects", { headers: getAuthHeaders() });
            if (subjectsRes.ok) {
                const subjects = await subjectsRes.json();
                const arr = Array.isArray(subjects) ? subjects : (subjects?.content || []);
                arr.forEach(s => {
                    if (s.id) subjectNamesCache[s.id] = s.name || s.subjectName || `Môn ${s.id}`;
                });
            }
        } catch (err) {
            console.error("Error loading subjects:", err);
        }
        
        // Load registrations cho từng giáo viên
        const pendingRows = await Promise.all(pendingList.map(async (t) => {
            let registrationsHtml = "";
            try {
                const regRes = await fetch(`/api/admin/teach/registrations/teacher/${t.id}`, {
                    headers: getAuthHeaders()
                });
                if (regRes.ok) {
                    const registrations = await regRes.json();
                    const pendingRegs = registrations.filter(r => r.status === "PENDING" && r.teachingClass == null);
                    
                    if (pendingRegs.length > 0) {
                        registrationsHtml = `
                            <tr class="registration-detail-row">
                                <td colspan="6" style="padding: 0; background: #f9fafb;">
                                    <div style="padding: 15px;">
                                        <strong style="color: #6366f1;">📚 Yêu cầu đăng ký mở lớp:</strong>
                                        <table style="width: 100%; margin-top: 10px; border-collapse: collapse;">
                                            <thead>
                                                <tr style="background: #e5e7eb;">
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Tên lớp</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Môn học</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Sĩ số</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Lịch học</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Hành động</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                ${pendingRegs.map(reg => {
                                                    const subjectName = reg.subjectId ? (subjectNamesCache[reg.subjectId] || `Môn ${reg.subjectId}`) : "-";
                                                    return `
                                                    <tr>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.className || "-")}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(subjectName)}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${reg.capacity || "-"}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.schedule || "-")}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">
                                                            <button class="btn btn-success" onclick="approveRegistrationAndCreateClass(${reg.id}, ${t.id}, '${escapeHtml(reg.className || '')}', ${reg.subjectId || 'null'}, ${reg.capacity || 'null'}); return false;">Duyệt & Tạo lớp</button>
                                                            <button class="btn btn-danger" onclick="rejectRegistration(${reg.id}, ${t.id}); return false;">Từ chối</button>
                                                        </td>
                                                    </tr>
                                                `;
                                                }).join("")}
                                            </tbody>
                                        </table>
                                    </div>
                                </td>
                            </tr>
                        `;
                    }
                }
            } catch (err) {
                console.error("Error loading registrations for teacher", t.id, err);
            }
            
            return `
                <tr>
                    <td>${(t.fullName || t.name || "-")}</td>
                    <td>${t.email ?? '-'}</td>
                    <td>${formatSubjects(t.subjects)}</td>
                    <td>-</td>
                    <td><span class="badge badge-pending">Chờ duyệt</span></td>
                    <td>
                        <div class="btn-group">
                            <button class="btn btn-info" onclick="viewTeacherProfile(${t.id})">Xem hồ sơ</button>
                            <button class="btn btn-success" onclick="approveTeacher(${t.id})">Duyệt</button>
                            <button class="btn btn-danger" onclick="rejectTeacher(${t.id})">Từ chối</button>
                        </div>
                    </td>
                </tr>
                ${registrationsHtml}
            `;
        }));
        
        pendingBody.innerHTML = pendingRows.length ? pendingRows.join("") : `<tr><td colspan="6" style="text-align:center;">Không có giáo viên</td></tr>`;

        // ====================== RENDER APPROVED ======================
        const approvedRows = await Promise.all(approvedList.map(async (t) => {
            let registrationsHtml = "";
            try {
                const regRes = await fetch(`/api/admin/teach/registrations/teacher/${t.id}`, {
                    headers: getAuthHeaders()
                });
                if (regRes.ok) {
                    const registrations = await regRes.json();
                    const pendingRegs = registrations.filter(r => r.status === "PENDING" && r.teachingClass == null);
                    
                    if (pendingRegs.length > 0) {
                        registrationsHtml = `
                            <tr class="registration-detail-row">
                                <td colspan="6" style="padding: 0; background: #f9fafb;">
                                    <div style="padding: 15px;">
                                        <strong style="color: #6366f1;">📚 Yêu cầu đăng ký mở lớp:</strong>
                                        <table style="width: 100%; margin-top: 10px; border-collapse: collapse;">
                                            <thead>
                                                <tr style="background: #e5e7eb;">
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Tên lớp</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Môn học</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Sĩ số</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Lịch học</th>
                                                    <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Hành động</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                ${pendingRegs.map(reg => {
                                                    const subjectName = reg.subjectId ? (subjectNamesCache[reg.subjectId] || `Môn ${reg.subjectId}`) : "-";
                                                    return `
                                                    <tr>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.className || "-")}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(subjectName)}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${reg.capacity || "-"}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.schedule || "-")}</td>
                                                        <td style="padding: 8px; border: 1px solid #d1d5db;">
                                                            <button class="btn btn-success" onclick="approveRegistrationAndCreateClass(${reg.id}, ${t.id}, '${escapeHtml(reg.className || '')}', ${reg.subjectId || 'null'}, ${reg.capacity || 'null'}); return false;">Duyệt & Tạo lớp</button>
                                                            <button class="btn btn-danger" onclick="rejectRegistration(${reg.id}, ${t.id}); return false;">Từ chối</button>
                                                        </td>
                                                    </tr>
                                                `;
                                                }).join("")}
                                            </tbody>
                                        </table>
                                    </div>
                                </td>
                            </tr>
                        `;
                    }
                }
            } catch (err) {
                console.error("Error loading registrations for teacher", t.id, err);
            }
            
            return `
                <tr>
                    <td>${(t.fullName || t.name || "-")}</td>
                    <td>${t.email ?? '-'}</td>
                    <td>${formatSubjects(t.subjects)}</td>
                    <td>${t.classCount ?? 0}</td>
                    <td><span class="badge badge-approved">Đã duyệt</span></td>
                    <td>
                        <div class="btn-group">
                            <button class="btn btn-info" onclick="viewTeacherProfile(${t.id})">Xem hồ sơ</button>
                            <button class="btn btn-danger" onclick="revokeTeacher(${t.id})">Hủy quyền</button>
                        </div>
                    </td>
                </tr>
                ${registrationsHtml}
            `;
        }));
        
        approvedBody.innerHTML = approvedRows.length ? approvedRows.join("") : `<tr><td colspan="6" style="text-align:center;">Không có giáo viên</td></tr>`;

        // ====================== RENDER REJECTED ======================
        rejectedBody.innerHTML = rejectedList.length ? rejectedList.map(t => `
            <tr>
                <td>${(t.fullName || t.name || "-")}</td>
                <td>${t.email ?? '-'}</td>
                <td>${formatSubjects(t.subjects)}</td>
                <td>${t.rejectReason ?? "Không có"}</td>
                <td><span class="badge badge-rejected">Từ chối</span></td>
                <td>
                    <button class="btn btn-info" onclick="viewTeacherProfile(${t.id})">Xem hồ sơ</button>
                </td>
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

// ====================== REGISTRATION ACTIONS ======================

async function approveRegistrationAndCreateClass(regId, teacherId, className, subjectId, capacity) {
    if (!confirm(`Bạn có chắc muốn duyệt giáo viên dạy môn này và tạo lớp "${className}"?`)) {
        return;
    }
    
    try {
        const userStr = localStorage.getItem("user");
        const user = userStr ? JSON.parse(userStr) : null;
        const adminId = user?.id || 1;
        
        const res = await fetch(`/api/admin/teach/registrations/${regId}/approve`, {
            method: "POST",
            headers: {
                ...getAuthHeaders(),
                "X-Admin-Id": String(adminId),
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                className: className || null,
                subjectId: subjectId ? Number(subjectId) : null,
                capacity: capacity ? Number(capacity) : null
            })
        });
        
        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Không thể duyệt và tạo lớp");
        }
        
        alert("Đã duyệt giáo viên và tạo lớp thành công!");
        await loadTeachers();
    } catch (err) {
        console.error("❌ approveRegistrationAndCreateClass error:", err);
        alert("Lỗi: " + (err.message || "Không thể duyệt và tạo lớp"));
    }
}

async function rejectRegistration(regId, teacherId) {
    const reason = prompt("Lý do từ chối yêu cầu đăng ký mở lớp:");
    if (reason === null) return;
    
    try {
        const userStr = localStorage.getItem("user");
        const user = userStr ? JSON.parse(userStr) : null;
        const adminId = user?.id || 1;
        
        const res = await fetch(`/api/admin/teach/registrations/${regId}/reject`, {
            method: "POST",
            headers: {
                ...getAuthHeaders(),
                "X-Admin-Id": String(adminId),
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ reason })
        });
        
        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || "Không thể từ chối");
        }
        
        alert("Đã từ chối yêu cầu!");
        await loadTeachers();
    } catch (err) {
        console.error("❌ rejectRegistration error:", err);
        alert("Lỗi: " + (err.message || "Không thể từ chối"));
    }
}

function escapeHtml(text) {
    if (!text) return "";
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

// getAuthHeaders được định nghĩa ở file khác (tms-api.js hoặc auth.js)

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

// ====================== VIEW TEACHER PROFILE ======================

async function viewTeacherProfile(teacherId) {
    console.log("🔍 Opening teacher profile for ID:", teacherId);
    try {
        // Load chi tiết giáo viên
        const teacherRes = await fetch(`/api/admin/teachers/${teacherId}`, {
            headers: getAuthHeaders()
        });
        
        if (!teacherRes.ok) {
            const errorText = await teacherRes.text();
            console.error("❌ Error loading teacher:", errorText);
            throw new Error("Không thể tải thông tin giáo viên");
        }
        
        const teacher = await teacherRes.json();
        
        // Load registrations
        let registrations = [];
        try {
            const regRes = await fetch(`/api/admin/teach/registrations/teacher/${teacherId}`, {
                headers: getAuthHeaders()
            });
            if (regRes.ok) {
                registrations = await regRes.json();
            }
        } catch (err) {
            console.error("Error loading registrations:", err);
        }
        
        // Load subjects để hiển thị tên môn học
        let subjectNamesCache = {};
        try {
            const subjectsRes = await fetch("/api/subjects", { headers: getAuthHeaders() });
            if (subjectsRes.ok) {
                const subjects = await subjectsRes.json();
                const arr = Array.isArray(subjects) ? subjects : (subjects?.content || []);
                arr.forEach(s => {
                    if (s.id) subjectNamesCache[s.id] = s.name || s.subjectName || `Môn ${s.id}`;
                });
            }
        } catch (err) {
            console.error("Error loading subjects:", err);
        }
        
        // Populate modal
        document.getElementById("modalName").textContent = teacher.fullName || teacher.name || "-";
        document.getElementById("modalEmail").textContent = teacher.email || "-";
        document.getElementById("modalPhone").textContent = teacher.phone || "-";
        document.getElementById("modalDob").textContent = teacher.dob ? new Date(teacher.dob).toLocaleDateString('vi-VN') : "-";
        document.getElementById("modalDegree").textContent = teacher.degree || "-";
        
        // Thêm các trường mới vào modal
        const modalContent = document.querySelector("#teacherModal .modal-content");
        
        // Tìm hoặc tạo các trường mới
        let modalExperience = document.getElementById("modalExperience");
        if (!modalExperience) {
            const expItem = document.createElement("div");
            expItem.className = "info-item";
            expItem.innerHTML = `
                <div class="info-label">Kinh nghiệm</div>
                <div class="info-value" id="modalExperience"></div>
            `;
            document.querySelector("#teacherModal .info-grid").appendChild(expItem);
            modalExperience = document.getElementById("modalExperience");
        }
        modalExperience.textContent = teacher.experience ? `${teacher.experience} năm` : "-";
        
        let modalAddress = document.getElementById("modalAddress");
        if (!modalAddress) {
            const addrItem = document.createElement("div");
            addrItem.className = "info-item";
            addrItem.innerHTML = `
                <div class="info-label">Địa chỉ</div>
                <div class="info-value" id="modalAddress"></div>
            `;
            document.querySelector("#teacherModal .info-grid").appendChild(addrItem);
            modalAddress = document.getElementById("modalAddress");
        }
        modalAddress.textContent = teacher.address || "-";
        
        let modalBio = document.getElementById("modalBio");
        if (!modalBio) {
            const bioItem = document.createElement("div");
            bioItem.className = "info-item";
            bioItem.style.gridColumn = "1 / -1"; // Full width
            bioItem.innerHTML = `
                <div class="info-label">Giới thiệu</div>
                <div class="info-value" id="modalBio" style="white-space: pre-wrap;"></div>
            `;
            document.querySelector("#teacherModal .info-grid").appendChild(bioItem);
            modalBio = document.getElementById("modalBio");
        }
        modalBio.textContent = teacher.bio || "-";
        
        // Cập nhật môn dạy
        const modalSubject = document.getElementById("modalSubject");
        if (teacher.subjects && teacher.subjects.length > 0) {
            modalSubject.textContent = teacher.subjects.map(s => {
                const status = s.active ? "✓" : "⏳";
                const name = s.name || s.code || `Môn ${s.subjectId}`;
                return `${status} ${name}`;
            }).join(", ");
        } else {
            modalSubject.textContent = teacher.mainSubject || "-";
        }
        
        // Hiển thị registrations
        let modalRegistrations = document.getElementById("modalRegistrations");
        if (!modalRegistrations) {
            const regItem = document.createElement("div");
            regItem.className = "info-item";
            regItem.style.gridColumn = "1 / -1"; // Full width
            regItem.innerHTML = `
                <div class="info-label">Yêu cầu đăng ký mở lớp</div>
                <div id="modalRegistrations" style="margin-top: 10px;"></div>
            `;
            document.querySelector("#teacherModal .info-grid").appendChild(regItem);
            modalRegistrations = document.getElementById("modalRegistrations");
        }
        
        const pendingRegs = registrations.filter(r => r.status === "PENDING" && r.teachingClass == null);
        if (pendingRegs.length > 0) {
            modalRegistrations.innerHTML = `
                <table style="width: 100%; border-collapse: collapse; margin-top: 10px;">
                    <thead>
                        <tr style="background: #e5e7eb;">
                            <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Tên lớp</th>
                            <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Môn học</th>
                            <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Sĩ số</th>
                            <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Lịch học</th>
                            <th style="padding: 8px; text-align: left; border: 1px solid #d1d5db;">Ghi chú</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${pendingRegs.map(reg => {
                            const subjectName = reg.subjectId ? (subjectNamesCache[reg.subjectId] || `Môn ${reg.subjectId}`) : "-";
                            return `
                            <tr>
                                <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.className || "-")}</td>
                                <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(subjectName)}</td>
                                <td style="padding: 8px; border: 1px solid #d1d5db;">${reg.capacity || "-"}</td>
                                <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.schedule || "-")}</td>
                                <td style="padding: 8px; border: 1px solid #d1d5db;">${escapeHtml(reg.requestNote || "-")}</td>
                            </tr>
                            `;
                        }).join("")}
                    </tbody>
                </table>
            `;
        } else {
            modalRegistrations.innerHTML = "<p style='color: #64748b;'>Không có yêu cầu đăng ký mở lớp nào</p>";
        }
        
        // Hiển thị modal
        document.getElementById("teacherModal").classList.add("show");
    } catch (err) {
        console.error("Error loading teacher profile:", err);
        alert("Lỗi: " + (err.message || "Không thể tải thông tin giáo viên"));
    }
}

function closeModal() {
    document.getElementById("teacherModal").classList.remove("show");
}

// Đóng modal khi click bên ngoài
document.addEventListener("click", (e) => {
    const modal = document.getElementById("teacherModal");
    if (e.target === modal) {
        closeModal();
    }
});

// Expose functions to window for onclick handlers
window.viewTeacherProfile = viewTeacherProfile;
window.closeModal = closeModal;

// ====================== SEARCH & FILTER ======================

function searchTeachers() {
    const query = document.getElementById("searchInput")?.value?.trim() || "";
    // Reload với query
    loadTeachers();
}

function filterBySubject() {
    const subject = document.getElementById("subjectFilter")?.value || "";
    // Reload với filter
    loadTeachers();
}
