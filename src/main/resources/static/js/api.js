/* ═══════════════════════════════════════════════════════════════
   API Client — Connects frontend to Spring Boot backend
   ═══════════════════════════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080/api';

const Api = {
    getToken() {
        return localStorage.getItem('jwt_token');
    },

    getCurrentUser() {
        const data = localStorage.getItem('user_data');
        return data ? JSON.parse(data) : null;
    },

    setAuth(data) {
        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('user_data', JSON.stringify({
            email: data.email,
            name: data.name,
            role: data.role
        }));
    },

    clearAuth() {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_data');
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            // Handle 401 — token expired or invalid
            if (response.status === 401) {
                this.clearAuth();
                window.location.href = 'index.html';
                return null;
            }

            const text = await response.text();
            const data = text ? JSON.parse(text) : null;

            if (!response.ok) {
                const errorMsg = data?.message || `Error ${response.status}`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                throw new Error('Cannot connect to server. Is the backend running on port 8080?');
            }
            throw error;
        }
    },

    // ── Auth ─────────────────────────────────────────────────
    login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    register(name, email, password) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, password })
        });
    },

    // ── Dashboard ────────────────────────────────────────────
    getDashboardSummary() {
        return this.request('/dashboard/summary');
    },

    getCategorySummary() {
        return this.request('/dashboard/category-summary');
    },

    getMonthlyTrends() {
        return this.request('/dashboard/monthly-trends');
    },

    getRecentActivity() {
        return this.request('/dashboard/recent-activity');
    },

    // ── Records ──────────────────────────────────────────────
    getRecords(params = {}) {
        const query = new URLSearchParams();
        if (params.type) query.set('type', params.type);
        if (params.category) query.set('category', params.category);
        if (params.startDate) query.set('startDate', params.startDate);
        if (params.endDate) query.set('endDate', params.endDate);
        if (params.search) query.set('search', params.search);
        query.set('page', params.page || 0);
        query.set('size', params.size || 15);
        query.set('sort', 'date,desc');
        return this.request(`/records?${query.toString()}`);
    },

    getRecord(id) {
        return this.request(`/records/${id}`);
    },

    createRecord(data) {
        return this.request('/records', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateRecord(id, data) {
        return this.request(`/records/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    deleteRecord(id) {
        return this.request(`/records/${id}`, { method: 'DELETE' });
    },

    // ── Users ────────────────────────────────────────────────
    getUsers() {
        return this.request('/users');
    },

    getUser(id) {
        return this.request(`/users/${id}`);
    },

    createUser(data) {
        return this.request('/users', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateUser(id, data) {
        return this.request(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    updateUserStatus(id, status) {
        return this.request(`/users/${id}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    },

    deleteUser(id) {
        return this.request(`/users/${id}`, { method: 'DELETE' });
    }
};
