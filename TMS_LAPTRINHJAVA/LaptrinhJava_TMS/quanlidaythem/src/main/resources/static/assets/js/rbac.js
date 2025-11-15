// ============================================
// RBAC SYSTEM (FINAL)
// ============================================

const ROLES = {
  ADMIN: 'ADMIN',
  TEACHER: 'TEACHER',
  STUDENT: 'STUDENT',
  PARENT: 'PARENT'
};

const PERMISSIONS = {
  'dashboard:view': ['ADMIN', 'TEACHER'],

  'teachers:view': ['ADMIN'],
  'teachers:approve': ['ADMIN'],
  'teachers:reject': ['ADMIN'],
  'teachers:suspend': ['ADMIN'],
  'teachers:edit': ['ADMIN'],

  'classes:view': ['ADMIN', 'TEACHER'],
  'classes:create': ['ADMIN'],
  'classes:edit': ['ADMIN'],
  'classes:delete': ['ADMIN'],
  'classes:approve': ['ADMIN'],
  'classes:reject': ['ADMIN'],
  'classes:assign-teacher': ['ADMIN'],
  'classes:schedule': ['ADMIN'],

  'students:view': ['ADMIN', 'TEACHER'],
  'students:enroll': ['ADMIN'],
  'students:edit': ['ADMIN'],

  'fees:view': ['ADMIN'],
  'fees:edit': ['ADMIN'],
  'fees:approve-payment': ['ADMIN'],

  'reports:view': ['ADMIN'],
  'reports:export': ['ADMIN'],

  'settings:view': ['ADMIN'],
  'settings:edit': ['ADMIN']
};

class RBACSystem {
  constructor() {
    this.currentUser = null;
    this.currentRole = null;
    this.loadCurrentUser();
  }

  loadCurrentUser() {
    try {
      const userStr = localStorage.getItem("currentUser");
      const token = localStorage.getItem("authToken");

      if (!token) return;

      if (userStr) {
        this.currentUser = JSON.parse(userStr);
        this.currentRole = this.currentUser.role || 'STUDENT';
        console.log("✅ RBAC loaded:", this.currentRole);
      }
    } catch (e) {
      console.error("RBAC load error:", e);
    }
  }

  hasPermission(p) {
    const allow = PERMISSIONS[p];
    if (!allow) return false;
    return allow.includes(this.currentRole);
  }

  applyRoleBasedUI() {
    document.querySelectorAll("[data-permission]").forEach(el => {
      const p = el.getAttribute("data-permission");
      if (!this.hasPermission(p)) el.style.display = "none";
    });

    console.log("✅ RBAC UI applied");
  }
}

const RBAC = new RBACSystem();
window.RBAC = RBAC;
