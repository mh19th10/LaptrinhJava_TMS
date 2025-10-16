// assets/js/admin-api.js
(function (window) {
  class ApiService {
    constructor() {
      this.base = '/api';      // cùng origin: http://localhost:8080/api
      this.tokenKey = 'jwtToken';
    }
    _headers() {
      const t = localStorage.getItem(this.tokenKey) || '';
      return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + t
      };
    }
    async _call(path, method = 'GET', data) {
      const res = await fetch(this.base + path, {
        method,
        headers: this._headers(),
        body: method === 'GET' || method === 'DELETE' ? undefined : JSON.stringify(data || {})
      });
      if (res.status === 401 || res.status === 403) {
        throw new Error('UNAUTH');
      }
      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || ('HTTP ' + res.status));
      }
      if (res.status === 204) return null;
      return res.json();
    }

    // ==== Admin dashboard APIs (đổi path theo backend của bạn) ====
    getDashboardStats()      { return this._call('/admin/dashboard/stats'); }
    getPendingClasses()      { return this._call('/admin/classes/pending'); }
    approveClass(id)         { return this._call(`/admin/classes/${id}/approve`, 'POST', {}); }
    rejectClass(id)          { return this._call(`/admin/classes/${id}/reject`,  'POST', {}); }

    // stub cho các tab khác:
    getTeachers()            { return this._call('/admin/teachers'); }
    getStudents()            { return this._call('/admin/students'); }
    getClasses()             { return this._call('/admin/classes'); }
    getFees()                { return this._call('/admin/fees'); }
    getReports()             { return this._call('/admin/reports'); }
    getSettings()            { return this._call('/admin/settings'); }
  }

  // Gắn global
  window.ApiService = ApiService;

})(window);
