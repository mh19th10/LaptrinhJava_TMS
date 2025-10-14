// ==============================
// API SERVICE (được sử dụng từ api.js)
// ==============================
const apiService = new ApiService();

// ==============================
// DASHBOARD MANAGER
// ==============================
class DashboardManager {
    constructor() {
        this.apiService = apiService;
        this.stats = {};
        this.pendingClasses = [];
    }

    async init() {
        try {
            await this.loadDashboardStats();
            await this.loadPendingClasses();
            this.setupEventListeners();
        } catch (error) {
            this.showError('Khởi tạo dashboard thất bại: ' + error.message);
        }
    }

    async loadDashboardStats() {
        try {
            this.showLoading('dashboard-stats');
            this.stats = await this.apiService.getDashboardStats();
            this.updateStatsUI();
        } catch (error) {
            this.showError('Không thể tải thống kê dashboard: ' + error.message);
        } finally {
            this.hideLoading('dashboard-stats');
        }
    }

    async loadPendingClasses() {
        try {
            this.showLoading('pending-classes');
            this.pendingClasses = await this.apiService.getPendingClasses();
            this.renderPendingClassesTable();
        } catch (error) {
            this.showError('Không thể tải danh sách lớp chờ duyệt: ' + error.message);
        } finally {
            this.hideLoading('pending-classes');
        }
    }

    updateStatsUI() {
        const s = this.stats || {};
        document.getElementById('total-teachers').textContent = s.totalTeachers || '0';
        document.getElementById('approved-teachers').textContent = s.approvedTeachers || '0';
        document.getElementById('total-students').textContent = s.totalStudents || '0';
        document.getElementById('active-students').textContent = s.activeStudents || '0';
        document.getElementById('total-classes').textContent = s.totalClasses || '0';
        document.getElementById('active-classes').textContent = s.activeClasses || '0';

        const revenueElement = document.getElementById('monthly-revenue');
        revenueElement.textContent = s.monthlyRevenue ? this.formatCurrency(s.monthlyRevenue) : '0 VNĐ';

        const changeElement = document.getElementById('revenue-change');
        if (s.revenueChange !== undefined && s.revenueChange !== null) {
            changeElement.textContent = (s.revenueChange > 0 ? '+' : '') + s.revenueChange + '%';
            changeElement.className = s.revenueChange >= 0 ? 'positive' : 'negative';
        } else {
            changeElement.textContent = '0%';
            changeElement.className = 'positive';
        }
    }

    renderPendingClassesTable() {
        const tableBody = document.querySelector('#pending-classes-table tbody');
        tableBody.innerHTML = '';

        if (this.pendingClasses?.length > 0) {
            this.pendingClasses.forEach(cls => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${cls.name || '--'}</td>
                    <td>${cls.teacherName || cls.teacher || '--'}</td>
                    <td>${cls.subject || '--'}</td>
                    <td>${cls.createdDate ? this.formatDate(cls.createdDate) : '--'}</td>
                    <td><span class="badge badge-warning">Chờ duyệt</span></td>
                    <td>
                        <button class="btn btn-approve" data-id="${cls.id}">Duyệt</button>
                        <button class="btn btn-reject" data-id="${cls.id}">Từ chối</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        } else {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="6" style="text-align: center;">Không có lớp học nào chờ duyệt</td>`;
            tableBody.appendChild(row);
        }
    }

    setupEventListeners() {
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-approve')) {
                this.approveClass(e.target.dataset.id);
            } else if (e.target.classList.contains('btn-reject')) {
                this.rejectClass(e.target.dataset.id);
            }
        });
    }

    async approveClass(classId) {
        if (!confirm('Bạn có chắc muốn phê duyệt lớp học này?')) return;
        try {
            await this.apiService.approveClass(classId);
            this.showSuccess('Đã phê duyệt lớp học thành công');
            await this.loadPendingClasses();
            await this.loadDashboardStats();
        } catch (error) {
            this.showError('Không thể phê duyệt lớp học: ' + error.message);
        }
    }

    async rejectClass(classId) {
        if (!confirm('Bạn có chắc muốn từ chối lớp học này?')) return;
        try {
            await this.apiService.rejectClass(classId);
            this.showSuccess('Đã từ chối lớp học thành công');
            await this.loadPendingClasses();
        } catch (error) {
            this.showError('Không thể từ chối lớp học: ' + error.message);
        }
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    formatDate(dateString) {
        try {
            return new Date(dateString).toLocaleDateString('vi-VN');
        } catch {
            return '--';
        }
    }

    showLoading(id) {
        const el = document.getElementById(id);
        if (el) el.classList.add('loading');
    }

    hideLoading(id) {
        const el = document.getElementById(id);
        if (el) el.classList.remove('loading');
    }

    showError(msg) {
        console.error(msg);
        alert('Lỗi: ' + msg);
    }

    showSuccess(msg) {
        console.log(msg);
        alert('Thành công: ' + msg);
    }
}

// ==============================
// TMS APPLICATION (GỘP DASHBOARD + NAVIGATION + ROLE ACCESS)
// ==============================
class TMSApplication {
    constructor() {
        this.dashboardManager = new DashboardManager();
        this.currentSection = 'dashboard';
    }

    async init() {
        this.checkAuthentication();
        this.setupNavigation();
        this.setupCurrentDate();
        this.setupUserInfo();
        await this.dashboardManager.init();

        // Làm mới dashboard mỗi 30 giây
        setInterval(() => {
            if (this.currentSection === 'dashboard') {
                this.dashboardManager.loadDashboardStats();
            }
        }, 30000);
    }

    checkAuthentication() {
        const token = localStorage.getItem('authToken');
        const userData = localStorage.getItem('currentUser');
        if (!token || !userData) {
            window.location.href = '/login.html';
            return;
        }
        this.setupRoleBasedAccess();
    }

    setupRoleBasedAccess() {
        const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
        if (user.role !== 'ADMIN') {
            document.querySelectorAll('[data-role="admin"]').forEach(el => {
                el.style.display = 'none';
            });
        }
    }

    setupNavigation() {
        document.querySelectorAll('.menu-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const target = item.getAttribute('data-target');
                if (!target) return;

                document.querySelectorAll('.menu-item').forEach(i => i.classList.remove('active'));
                item.classList.add('active');
                document.querySelectorAll('.content-section').forEach(sec => sec.classList.remove('active'));

                const section = document.getElementById(target);
                if (section) {
                    section.classList.add('active');
                    this.currentSection = target;
                    this.loadSectionData(target);
                }
            });
        });
    }

    setupCurrentDate() {
        const el = document.getElementById('current-date');
        if (el) {
            el.textContent = new Date().toLocaleDateString('vi-VN', {
                weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
            });
        }
    }

    setupUserInfo() {
        if (window.userManager) window.userManager.updateUserInfo();
    }

    async loadSectionData(section) {
        try {
            switch(section) {
                case 'teachers':
                    console.log('Tải dữ liệu giáo viên...');
                    await this.dashboardManager.apiService.getTeachers();
                    break;
                case 'students':
                    console.log('Tải dữ liệu học sinh...');
                    await this.dashboardManager.apiService.getStudents();
                    break;
                case 'classes':
                    console.log('Tải dữ liệu lớp học...');
                    await this.dashboardManager.apiService.getClasses();
                    break;
                case 'fees':
                    console.log('Tải dữ liệu học phí...');
                    await this.dashboardManager.apiService.getFees();
                    break;
                case 'reports':
                    console.log('Tải dữ liệu báo cáo...');
                    await this.dashboardManager.apiService.getReports();
                    break;
                case 'settings':
                    console.log('Tải cài đặt hệ thống...');
                    await this.dashboardManager.apiService.getSettings();
                    break;
            }
        } catch (error) {
            console.error('Lỗi khi tải dữ liệu section:', section, error);
        }
    }
}

// ==============================
// INITIALIZE APPLICATION
// ==============================
document.addEventListener('DOMContentLoaded', () => {
    const app = new TMSApplication();
    app.init();
});
