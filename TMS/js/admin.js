// admin.js - API Connection for Admin Portal
// Bích Thủy - TMS Project

// API Base URL (thay đổi theo môi trường)
const API_BASE_URL = 'http://localhost:8080/api';

// Utility Functions
const getAuthToken = () => {
  return localStorage.getItem('authToken');
};

const getAuthHeaders = () => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getAuthToken()}`
  };
};

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Có lỗi xảy ra');
  }
  return response.json();
};

// ==================== AUTHENTICATION ====================
const AuthAPI = {
  // Login
  login: async (email, password) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await handleResponse(response);
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('currentUser', JSON.stringify(data.user));
    return data;
  },

  // Logout
  logout: async () => {
    await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  },

  // Check authentication
  checkAuth: () => {
    const token = getAuthToken();
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    if (!token || user.role !== 'ADMIN') {
      window.location.href = 'login.html';
      return false;
    }
    return true;
  }
};

// ==================== DASHBOARD ====================
const DashboardAPI = {
  // Get statistics
  getStatistics: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/dashboard/statistics`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get recent activities
  getRecentActivities: async (limit = 10) => {
    const response = await fetch(`${API_BASE_URL}/admin/dashboard/activities?limit=${limit}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get pending approvals
  getPendingApprovals: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/dashboard/pending`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  }
};

// ==================== TEACHER MANAGEMENT ====================
const TeacherAPI = {
  // Get all teachers
  getAllTeachers: async (status = '', subject = '') => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (subject) params.append('subject', subject);
    
    const response = await fetch(`${API_BASE_URL}/admin/teachers?${params}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get teacher by ID
  getTeacherById: async (teacherId) => {
    const response = await fetch(`${API_BASE_URL}/admin/teachers/${teacherId}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Approve teacher
  approveTeacher: async (teacherId) => {
    const response = await fetch(`${API_BASE_URL}/admin/teachers/${teacherId}/approve`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Reject teacher
  rejectTeacher: async (teacherId, reason) => {
    const response = await fetch(`${API_BASE_URL}/admin/teachers/${teacherId}/reject`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason })
    });
    return handleResponse(response);
  },

  // Suspend teacher
  suspendTeacher: async (teacherId, reason) => {
    const response = await fetch(`${API_BASE_URL}/admin/teachers/${teacherId}/suspend`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason })
    });
    return handleResponse(response);
  },

  // Search teachers
  searchTeachers: async (keyword) => {
    const response = await fetch(`${API_BASE_URL}/admin/teachers/search?q=${keyword}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  }
};

// ==================== CLASS MANAGEMENT ====================
const ClassAPI = {
  // Get all classes
  getAllClasses: async (status = '', type = '', subject = '') => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (type) params.append('type', type);
    if (subject) params.append('subject', subject);
    
    const response = await fetch(`${API_BASE_URL}/admin/classes?${params}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get class by ID
  getClassById: async (classId) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/${classId}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Approve class
  approveClass: async (classId) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/${classId}/approve`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Reject class
  rejectClass: async (classId, reason) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/${classId}/reject`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason })
    });
    return handleResponse(response);
  },

  // Update class
  updateClass: async (classId, classData) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/${classId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(classData)
    });
    return handleResponse(response);
  },

  // Get class students
  getClassStudents: async (classId) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/${classId}/students`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Search classes
  searchClasses: async (keyword) => {
    const response = await fetch(`${API_BASE_URL}/admin/classes/search?q=${keyword}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  }
};

// ==================== FEE MANAGEMENT ====================
const FeeAPI = {
  // Get all fees
  getAllFees: async (status = '', month = '') => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (month) params.append('month', month);
    
    const response = await fetch(`${API_BASE_URL}/admin/fees?${params}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get fee by ID
  getFeeById: async (feeId) => {
    const response = await fetch(`${API_BASE_URL}/admin/fees/${feeId}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Mark payment as paid
  markAsPaid: async (feeId, amount, paymentMethod = 'CASH') => {
    const response = await fetch(`${API_BASE_URL}/admin/fees/${feeId}/pay`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, paymentMethod })
    });
    return handleResponse(response);
  },

  // Send payment reminder
  sendReminder: async (feeId) => {
    const response = await fetch(`${API_BASE_URL}/admin/fees/${feeId}/remind`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get fee statistics
  getFeeStatistics: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/fees/statistics`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Export fee report
  exportFeeReport: async (format = 'excel', filters = {}) => {
    const params = new URLSearchParams(filters);
    const response = await fetch(`${API_BASE_URL}/admin/fees/export?format=${format}&${params}`, {
      headers: getAuthHeaders()
    });
    
    if (!response.ok) throw new Error('Export failed');
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `fee_report_${Date.now()}.${format}`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
};

// ==================== REPORTS & STATISTICS ====================
const ReportAPI = {
  // Get summary statistics
  getSummaryStats: async (fromDate, toDate) => {
    const params = new URLSearchParams();
    if (fromDate) params.append('from', fromDate);
    if (toDate) params.append('to', toDate);
    
    const response = await fetch(`${API_BASE_URL}/admin/reports/summary?${params}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get growth data
  getGrowthData: async (period = 'monthly') => {
    const response = await fetch(`${API_BASE_URL}/admin/reports/growth?period=${period}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get subject distribution
  getSubjectDistribution: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/reports/subjects`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get revenue data
  getRevenueData: async (year) => {
    const response = await fetch(`${API_BASE_URL}/admin/reports/revenue?year=${year}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get top teachers
  getTopTeachers: async (limit = 10) => {
    const response = await fetch(`${API_BASE_URL}/admin/reports/top-teachers?limit=${limit}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Get compliance report (Thông tư 29)
  getComplianceReport: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/reports/compliance`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Export report
  exportReport: async (format = 'excel', reportType, filters = {}) => {
    const params = new URLSearchParams(filters);
    params.append('type', reportType);
    
    const response = await fetch(`${API_BASE_URL}/admin/reports/export?format=${format}&${params}`, {
      headers: getAuthHeaders()
    });
    
    if (!response.ok) throw new Error('Export failed');
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `report_${reportType}_${Date.now()}.${format}`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
};

// ==================== SETTINGS ====================
const SettingsAPI = {
  // Get system settings
  getSystemSettings: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/settings`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Update system info
  updateSystemInfo: async (data) => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/system`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  // Update system config
  updateConfig: async (config) => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/config`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(config)
    });
    return handleResponse(response);
  },

  // Update fee config
  updateFeeConfig: async (config) => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/fee-config`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(config)
    });
    return handleResponse(response);
  },

  // Update class config
  updateClassConfig: async (config) => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/class-config`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(config)
    });
    return handleResponse(response);
  },

  // Create backup
  createBackup: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/backup/create`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Restore backup
  restoreBackup: async (backupId) => {
    const response = await fetch(`${API_BASE_URL}/admin/backup/restore`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ backupId })
    });
    return handleResponse(response);
  },

  // Clear cache
  clearCache: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/cache/clear`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Reset system
  resetSystem: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/system/reset`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Delete all data
  deleteAllData: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/data/delete-all`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  }
};

// ==================== NOTIFICATIONS ====================
const NotificationAPI = {
  // Get all notifications
  getNotifications: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/notifications`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // Send notification
  sendNotification: async (data) => {
    const response = await fetch(`${API_BASE_URL}/admin/notifications/send`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  // Mark as read
  markAsRead: async (notificationId) => {
    const response = await fetch(`${API_BASE_URL}/admin/notifications/${notificationId}/read`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  }
};

// Export all APIs
const TMS_Admin_API = {
  Auth: AuthAPI,
  Dashboard: DashboardAPI,
  Teacher: TeacherAPI,
  Class: ClassAPI,
  Fee: FeeAPI,
  Report: ReportAPI,
  Settings: SettingsAPI,
  Notification: NotificationAPI
};

// Make it available globally
if (typeof window !== 'undefined') {
  window.TMS_API = TMS_Admin_API;
}

// Export for ES6 modules
export default TMS_Admin_API;