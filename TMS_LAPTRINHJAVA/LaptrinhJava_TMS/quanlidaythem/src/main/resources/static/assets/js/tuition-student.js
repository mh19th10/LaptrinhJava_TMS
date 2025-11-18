// tuition-student.js - Quản lý học phí và thanh toán cho học sinh

let currentPaymentTransactionId = null;
let paymentPollingInterval = null;

document.addEventListener('DOMContentLoaded', async () => {
    if (!requireLoginOrRedirect()) return;
    if (!requireRoleOrRedirect('STUDENT')) return;

    wireLogout();
    await loadFees();
    
    // Kiểm tra payment đang pending khi load trang
    await checkPendingPayments();
});

// ====================== LOAD FEES ======================

async function loadFees() {
    const tbody = document.getElementById('feesTableBody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Đang tải dữ liệu...</td></tr>';

    try {
        const fees = await TMS_API.Fees.getAll();
        console.log('📌 Fees:', fees);

        if (!fees || fees.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Chưa có học phí nào</td></tr>';
            return;
        }

        tbody.innerHTML = fees.map(f => {
            const amount = f.amount || 0;
            const paid = f.paid || 0;
            const remaining = amount - paid;
            const isPaid = paid >= amount;

            // Trạng thái
            let statusBadge = '';
            if (isPaid) {
                statusBadge = '<span class="badge success">Đã thanh toán</span>';
            } else if (paid > 0) {
                statusBadge = '<span class="badge warn">Thanh toán một phần</span>';
            } else {
                statusBadge = '<span class="badge warn">Chưa thanh toán</span>';
            }

            // Hành động
            let actions = '';
            if (!isPaid && remaining > 0) {
                actions = `<button class="btn-pay" onclick="openPaymentModal(${f.id}, '${f.className || ''}', '${f.month || ''}', ${remaining}, '${f.studentName || ''}')">Thanh toán</button>`;
            } else {
                actions = '<span style="color:#999;">—</span>';
            }

            return `
                <tr>
                    <td>${f.className || '—'}</td>
                    <td>${f.month || '—'}</td>
                    <td>${formatCurrency(amount)}</td>
                    <td>${formatCurrency(paid)}</td>
                    <td>${statusBadge}</td>
                    <td>${actions}</td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        console.error('❌ Error loading fees:', err);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#c00;">Lỗi tải dữ liệu. Vui lòng thử lại.</td></tr>';
    }
}

// ====================== PAYMENT MODAL ======================

async function openPaymentModal(feeId, className, month, amount, studentName) {
    const modal = document.getElementById('paymentModal');
    const qrCodeLoading = document.getElementById('qrCodeLoading');
    const qrCodeImage = document.getElementById('qrCodeImage');
    const paymentStatusText = document.getElementById('paymentStatusText');
    const transactionIdText = document.getElementById('transactionIdText');
    const checkStatusBtn = document.getElementById('checkStatusBtn');

    // Hiển thị thông tin
    document.getElementById('paymentClassName').textContent = className || '—';
    document.getElementById('paymentMonth').textContent = month || '—';
    document.getElementById('paymentAmount').textContent = formatCurrency(amount);

    // Reset UI
    qrCodeLoading.style.display = 'block';
    qrCodeImage.style.display = 'none';
    paymentStatusText.textContent = 'Đang chờ thanh toán';
    paymentStatusText.className = 'badge warn';
    transactionIdText.textContent = '';
    checkStatusBtn.style.display = 'none';
    currentPaymentTransactionId = null;

    // Hiển thị modal
    modal.style.display = 'flex';

    try {
        // Tạo payment và lấy QR code
        const payment = await TMS_API.Payments.create(
            amount,
            feeId,
            studentName,
            `Thanh toán học phí - ${className} - ${month}`
        );

        console.log('✅ Payment created:', payment);

        // Lưu transactionId
        currentPaymentTransactionId = payment.transactionId;
        // Lưu vào localStorage để có thể check lại sau khi reload
        localStorage.setItem('pendingPaymentTransactionId', payment.transactionId);

        // Hiển thị QR code
        if (payment.qrCodeBase64) {
            qrCodeImage.src = payment.qrCodeBase64;
            qrCodeImage.style.display = 'block';
            qrCodeLoading.style.display = 'none';
        }

        // Hiển thị transaction ID
        transactionIdText.textContent = `Mã giao dịch: ${payment.transactionId}`;

        // Bắt đầu polling
        startPaymentPolling(payment.transactionId);

    } catch (error) {
        console.error('❌ Error creating payment:', error);
        qrCodeLoading.innerHTML = `<div style="color:#c00;">Lỗi tạo mã thanh toán: ${error.message || 'Vui lòng thử lại'}</div>`;
    }
}

function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    modal.style.display = 'none';
    // KHÔNG dừng polling - để tiếp tục kiểm tra ngay cả khi modal đóng
    // stopPaymentPolling();
    // currentPaymentTransactionId = null; // Giữ lại để có thể check sau
}

// ====================== PAYMENT POLLING ======================

function startPaymentPolling(transactionId) {
    // Dừng polling cũ nếu có
    stopPaymentPolling();

    // Kiểm tra trạng thái mỗi 3 giây
    paymentPollingInterval = setInterval(async () => {
        try {
            const payment = await TMS_API.Payments.getStatus(transactionId);
            
            const statusText = document.getElementById('paymentStatusText');
            const checkStatusBtn = document.getElementById('checkStatusBtn');

            if (payment.status === 'SUCCESS') {
                statusText.textContent = 'Thanh toán thành công';
                statusText.className = 'badge success';
                stopPaymentPolling();
                checkStatusBtn.style.display = 'none';
                
                // Xóa pending transactionId
                localStorage.removeItem('pendingPaymentTransactionId');
                
                // Reload fees và hiển thị thông báo
                loadFees();
                
                // Hiển thị thông báo thành công
                showSuccessNotification('✅ Thanh toán thành công! Trang sẽ tự động reload...');
                
                // Tự động reload trang sau 2 giây để đảm bảo UI cập nhật
                setTimeout(() => {
                    location.reload();
                }, 2000);
            } else if (payment.status === 'FAILED') {
                statusText.textContent = 'Thanh toán thất bại';
                statusText.className = 'badge';
                statusText.style.background = '#f44336';
                statusText.style.color = 'white';
                stopPaymentPolling();
                checkStatusBtn.style.display = 'block';
            } else {
                statusText.textContent = 'Đang chờ thanh toán';
                statusText.className = 'badge warn';
            }
        } catch (error) {
            console.error('❌ Error checking payment status:', error);
        }
    }, 3000);
}

function stopPaymentPolling() {
    if (paymentPollingInterval) {
        clearInterval(paymentPollingInterval);
        paymentPollingInterval = null;
    }
}

async function checkPaymentStatus() {
    if (!currentPaymentTransactionId) return;
    
    try {
        const payment = await TMS_API.Payments.getStatus(currentPaymentTransactionId);
        const statusText = document.getElementById('paymentStatusText');
        
        if (payment.status === 'SUCCESS') {
            statusText.textContent = 'Thanh toán thành công';
            statusText.className = 'badge success';
            stopPaymentPolling();
            document.getElementById('checkStatusBtn').style.display = 'none';
            
            // Reload fees và hiển thị thông báo
            loadFees();
            showSuccessNotification('✅ Thanh toán thành công! Trang sẽ tự động reload...');
            
            // Tự động reload trang sau 2 giây
            setTimeout(() => {
                location.reload();
            }, 2000);
        } else if (payment.status === 'FAILED') {
            statusText.textContent = 'Thanh toán thất bại';
            statusText.className = 'badge';
            statusText.style.background = '#f44336';
            statusText.style.color = 'white';
        } else {
            statusText.textContent = 'Đang chờ thanh toán';
            statusText.className = 'badge warn';
            // Tiếp tục polling
            startPaymentPolling(currentPaymentTransactionId);
        }
    } catch (error) {
        console.error('❌ Error checking payment status:', error);
        alert('Lỗi kiểm tra trạng thái thanh toán');
    }
}

// ====================== HELPERS ======================

function formatCurrency(v) {
    if (!v && v !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(v) + ' ₫';
}

// Hiển thị thông báo thành công
function showSuccessNotification(message) {
    // Tạo notification element
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 16px 24px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        z-index: 10000;
        font-size: 16px;
        font-weight: 500;
        animation: slideIn 0.3s ease-out;
    `;
    notification.textContent = message;
    
    // Thêm animation CSS nếu chưa có
    if (!document.getElementById('notification-style')) {
        const style = document.createElement('style');
        style.id = 'notification-style';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);
    }
    
    document.body.appendChild(notification);
    
    // Tự động xóa sau 5 giây
    setTimeout(() => {
        notification.style.animation = 'slideIn 0.3s ease-out reverse';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
}

// Kiểm tra payment đang pending khi load trang
async function checkPendingPayments() {
    try {
        // Lấy danh sách fees để xem có payment nào đang pending không
        const fees = await TMS_API.Fees.getAll();
        
        // Kiểm tra trong localStorage xem có transactionId đang pending không
        const pendingTransactionId = localStorage.getItem('pendingPaymentTransactionId');
        
        if (pendingTransactionId) {
            console.log('🔍 Phát hiện payment đang pending:', pendingTransactionId);
            // Kiểm tra status
            const payment = await TMS_API.Payments.getStatus(pendingTransactionId);
            
            if (payment.status === 'SUCCESS') {
                // Thanh toán đã thành công, reload trang
                localStorage.removeItem('pendingPaymentTransactionId');
                showSuccessNotification('✅ Thanh toán đã được xác nhận!');
                setTimeout(() => location.reload(), 1500);
            } else if (payment.status === 'PENDING') {
                // Vẫn đang pending, tiếp tục polling
                currentPaymentTransactionId = pendingTransactionId;
                startPaymentPolling(pendingTransactionId);
            }
        }
    } catch (error) {
        console.error('❌ Error checking pending payments:', error);
    }
}

// Đóng modal khi click bên ngoài
window.onclick = function(event) {
    const modal = document.getElementById('paymentModal');
    if (event.target === modal) {
        closePaymentModal();
    }
}

