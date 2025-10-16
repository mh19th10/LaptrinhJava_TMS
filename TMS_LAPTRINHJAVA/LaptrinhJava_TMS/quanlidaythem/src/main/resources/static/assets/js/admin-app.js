// assets/js/admin-app.js
(function () {

  // Utils hiển thị tên viết tắt avatar
  function getAbbreviatedName(fullName) {
    if (!fullName) return '👤';
    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  class DashboardManager {
    constructor(api) {
      this.api = api;
      this.stats = {};
      this.pending = [];
    }

    async init() {
      await this.loadStats();
      await this.loadPending();
      this._wireRowActions();
      document.getElementById('current-date').textContent =
        new Date().toLocaleDateString('vi-VN');
    }

    async loadStats() {
      try {
        this.stats = await this.api.getDashboardStats();
        const s = this.stats || {};
        q('#total-teachers').textContent   = s.totalTeachers   ?? '--';
        q('#approved-teachers').textContent= s.approvedTeachers ?? '--';
        q('#total-students').textContent   = s.totalStudents   ?? '--';
        q('#active-students').textContent  = s.activeStudents  ?? '--';
        q('#total-classes').textContent    = s.totalClasses    ?? '--';
        q('#active-classes').textContent   = s.activeClasses   ?? '--';
        q('#monthly-revenue').textContent  = s.monthlyRevenue ? (Number(s.monthlyRevenue).toLocaleString('vi-VN') + ' VNĐ') : '-- VNĐ';
        q('#revenue-change').textContent   = (s.revenueChange ?? '--') + '%';
      } catch (e) {
        console.error(e);
        alert('Không thể tải thống kê.');
      }
    }

    async loadPending() {
      try {
        this.pending = await this.api.getPendingClasses();
        const tbody = q('#pending-classes-table tbody');
        tbody.innerHTML = '';
        if (Array.isArray(this.pending) && this.pending.length) {
          this.pending.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
              <td>${c.name ?? '--'}</td>
              <td>${c.teacherName ?? c.teacher ?? '--'}</td>
              <td>${c.subject ?? '--'}</td>
              <td>${c.createdDate ? new Date(c.createdDate).toLocaleDateString('vi-VN') : '--'}</td>
              <td><span class="badge badge-warning">Chờ duyệt</span></td>
              <td>
                <button class="btn-approve" data-id="${c.id}">Duyệt</button>
                <button class="btn-reject"  data-id="${c.id}">Từ chối</button>
              </td>`;
            tbody.appendChild(tr);
          });
        } else {
          const tr = document.createElement('tr');
          tr.innerHTML = `<td colspan="6" style="text-align:center">Không có lớp chờ duyệt</td>`;
          tbody.appendChild(tr);
        }
      } catch (e) {
        console.error(e);
        alert('Không thể tải danh sách lớp chờ duyệt.');
      }
    }

    _wireRowActions() {
      document.addEventListener('click', async (ev) => {
        const btn = ev.target.closest('.btn-approve, .btn-reject');
        if (!btn) return;
        const id = btn.dataset.id;
        try {
          if (btn.classList.contains('btn-approve')) {
            if (!confirm('Duyệt lớp này?')) return;
            await this.api.approveClass(id);
            alert('Đã duyệt.');
          } else {
            if (!confirm('Từ chối lớp này?')) return;
            await this.api.rejectClass(id);
            alert('Đã từ chối.');
          }
          await this.loadPending();
          await this.loadStats();
        } catch (e) {
          alert('Thao tác thất bại: ' + e.message);
        }
      });
    }
  }

  class TMSApp {
    constructor() {
      this.api = new ApiService();
      this.dashboard = new DashboardManager(this.api);
    }

    async start() {
      // Guard đăng nhập & role
      const token = localStorage.getItem('jwtToken');
      const user  = JSON.parse(localStorage.getItem('currentUser') || '{}');
      if (!token) { location.href = 'login.html'; return; }
      if ((user.role || '').toUpperCase() !== 'ADMIN') {
        // tuỳ bạn điều hướng về trang tương ứng role
        location.href = 'dashboard.html';
        return;
      }

      // Avatar + tên
      q('#user-display-name').textContent = user.fullName || 'Quản trị viên';
      q('#user-avatar').textContent = getAbbreviatedName(user.fullName);

      // Nav
      document.querySelectorAll('.menu-item').forEach(it => {
        it.addEventListener('click', () => {
          document.querySelectorAll('.menu-item').forEach(x => x.classList.remove('active'));
          it.classList.add('active');
          const target = it.getAttribute('data-target');
          document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
          const sec = document.getElementById(target);
          if (sec) sec.classList.add('active');
        });
      });

      // Init dash
      await this.dashboard.init();

      // Auto refresh stats mỗi 30s (khi đang ở dashboard)
      setInterval(() => {
        if (document.getElementById('dashboard').classList.contains('active')) {
          this.dashboard.loadStats();
        }
      }, 30000);
    }
  }

  function q(sel) { return document.querySelector(sel); }

  document.addEventListener('DOMContentLoaded', () => {
    new TMSApp().start();
  });

})();
