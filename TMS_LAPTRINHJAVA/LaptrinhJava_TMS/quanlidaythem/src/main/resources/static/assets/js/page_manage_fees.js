// ======================================
// PAGE: MANAGE FEES
// ======================================

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireLoginOrRedirect()) return;
    if (!requireRoleOrRedirect("ADMIN")) return;
    RBAC.applyRoleBasedUI();
    loadFees();
});

// ====================== LOAD FEES ======================

async function loadFees() {
    const tbody = document.getElementById("feesTableBody");
    tbody.innerHTML = `<tr><td colspan="9">Đang tải dữ liệu...</td></tr>`;

    try {
        const fees = await TMS_API.Fees.getAll();
        console.log("📌 Fees:", fees);

        if (!fees || fees.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" style="text-align:center">Không có dữ liệu học phí</td></tr>`;
            return;
        }

        tbody.innerHTML = fees.map(f => {

            const remaining = f.amount - f.paid;

            // --- Hạn đóng (nếu đã đóng thì ghi "Đã đóng") ---
            const dueDateDisplay = f.paid >= f.amount
                ? "Đã đóng"
                : (f.dueDate ? f.dueDate : "—");

            // --- Trạng thái ---
            const statusBadge = f.paid >= f.amount
                ? `<span class="badge badge-paid">Đã đóng</span>`
                : `<span class="badge badge-pending">Còn nợ</span>`;

            // --- Hành động ---
            const actions = `
                <button class="btn btn-info" onclick="viewFee(${f.id})">Chi tiết</button>
            `;

            return `
                <tr>
                    <td>${f.studentName}</td>
                    <td>${f.className}</td>
                    <td>${f.month}</td>
                    <td>${formatCurrency(f.amount)}</td>
                    <td>${formatCurrency(f.paid)}</td>
                    <td>${formatCurrency(remaining)}</td>
                    <td>${dueDateDisplay}</td>
                    <td>${statusBadge}</td>
                    <td>${actions}</td>
                </tr>
            `;
        }).join("");

    } catch (err) {
        console.error("❌ Error loading fees:", err);
        tbody.innerHTML = `<tr><td colspan="9">Lỗi tải dữ liệu</td></tr>`;
    }
}

// ====================== HELPERS ======================

function formatCurrency(v) {
    return new Intl.NumberFormat("vi-VN").format(v) + " ₫";
}

function viewFee(id) {
    alert("Chi tiết học phí ID: " + id);
}
