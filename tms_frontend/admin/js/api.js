// API Configuration
const API_BASE_URL = 'https://your-api-domain.com/api';

// API Endpoints
const API_ENDPOINTS = {
    DASHBOARD_STATS: '/dashboard/stats',
    TEACHERS: '/teachers',
    STUDENTS: '/students',
    CLASSES: '/classes',
    PENDING_CLASSES: '/classes/pending',
    FEES: '/fees',
    REPORTS: '/reports'
};

// Generic API call function
async function apiCall(endpoint, method = 'GET', data = null) {
    const url = API_BASE_URL + endpoint;
    const options = {
        method: method,
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'Content-Type': 'application/json'
        }
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// Specific API functions
export const dashboardAPI = {
    getStats: () => apiCall(API_ENDPOINTS.DASHBOARD_STATS),
    getPendingClasses: () => apiCall(API_ENDPOINTS.PENDING_CLASSES)
};

export const teacherAPI = {
    getAll: () => apiCall(API_ENDPOINTS.TEACHERS),
    getById: (id) => apiCall(`${API_ENDPOINTS.TEACHERS}/${id}`),
    approve: (id) => apiCall(`${API_ENDPOINTS.TEACHERS}/${id}/approve`, 'POST'),
    reject: (id) => apiCall(`${API_ENDPOINTS.TEACHERS}/${id}/reject`, 'POST')
};

export const classAPI = {
    getAll: () => apiCall(API_ENDPOINTS.CLASSES),
    approve: (id) => apiCall(`${API_ENDPOINTS.CLASSES}/${id}/approve`, 'POST'),
    reject: (id) => apiCall(`${API_ENDPOINTS.CLASSES}/${id}/reject`, 'POST')
};