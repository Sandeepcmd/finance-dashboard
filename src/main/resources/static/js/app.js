/* ═══════════════════════════════════════════════════════════════
   Finance Dashboard — Main Application Logic
   ═══════════════════════════════════════════════════════════════ */

// ── Auth Check ──────────────────────────────────────────────────
if (!Api.isLoggedIn()) {
    window.location.href = 'index.html';
}

const currentUser = Api.getCurrentUser();

// Force logout if old cache data is missing the role
if (!currentUser || !currentUser.role) {
    Api.clearAuth();
    window.location.href = 'index.html';
}

let currentPage = 0;
let searchTimeout = null;
let monthlyChartInstance = null;
let categoryChartInstance = null;

// ── Initialize ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    setupUI();
    loadDashboard();
});

function setupUI() {
    // Set user info
    document.getElementById('userName').textContent = currentUser.name;
    document.getElementById('userRole').textContent = currentUser.role;
    document.getElementById('userAvatar').textContent = currentUser.name.charAt(0).toUpperCase();

    // Set current date
    const now = new Date();
    document.getElementById('currentDate').textContent = now.toLocaleDateString('en-US', {
        weekday: 'short', year: 'numeric', month: 'short', day: 'numeric'
    });

    // Role-based UI Customization
    const role = currentUser.role || 'VIEWER';
    if (role) {
        document.body.classList.add(`theme-${role.toLowerCase()}`);
    }

    if (role === 'ADMIN') {
        const adminBanner = document.getElementById('bannerAdmin');
        if(adminBanner) adminBanner.style.display = 'block';
        // Admin sees everything
    } else if (role === 'ANALYST') {
        const analystBanner = document.getElementById('bannerAnalyst');
        if (analystBanner) analystBanner.style.display = 'block';
        const navUsers = document.getElementById('navUsers');
        if (navUsers) navUsers.style.display = 'none';
        
        // Change summary cards layout
        const cardRecords = document.querySelector('.card-records');
        if (cardRecords) cardRecords.style.display = 'none';
        const summaryCards = document.querySelector('.summary-cards');
        if (summaryCards) summaryCards.style.gridTemplateColumns = 'repeat(3, 1fr)';
    } else {
        // Default viewer logic applies to 'VIEWER' and any unknown role
        const viewerBanner = document.getElementById('bannerViewer');
        if (viewerBanner) viewerBanner.style.display = 'block';
        
        const navRecords = document.getElementById('navRecords');
        if(navRecords) navRecords.style.display = 'none';
        
        const navUsers = document.getElementById('navUsers');
        if(navUsers) navUsers.style.display = 'none';
        
        // Simplify Dashboard for Viewer
        const recentActivity = document.querySelector('.recent-activity');
        if(recentActivity) recentActivity.style.display = 'none';
        
        const cardRecords = document.querySelector('.card-records');
        if(cardRecords) cardRecords.style.display = 'none';
        
        const summaryCards = document.querySelector('.summary-cards');
        if(summaryCards) summaryCards.style.gridTemplateColumns = 'repeat(3, 1fr)';
    }
}

// ═══════════════════════════════════════════════════════════════
//  NAVIGATION
// ═══════════════════════════════════════════════════════════════

function navigate(page, el) {
    event.preventDefault();

    // Update active nav
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    if (el) el.classList.add('active');

    // Hide all pages, show target
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const pageEl = document.getElementById('page' + capitalize(page));
    if (pageEl) pageEl.classList.add('active');

    // Update header
    const titles = {
        dashboard: ['Dashboard', 'Financial overview and analytics'],
        records: ['Financial Records', 'Manage income and expense records'],
        users: ['User Management', 'Manage users, roles, and access']
    };
    document.getElementById('pageTitle').textContent = titles[page]?.[0] || '';
    document.getElementById('pageSubtitle').textContent = titles[page]?.[1] || '';

    // Load page data
    if (page === 'dashboard') loadDashboard();
    else if (page === 'records') loadRecords();
    else if (page === 'users') loadUsers();

    // Close mobile sidebar
    document.getElementById('sidebar').classList.remove('mobile-open');
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('mobile-open');
    } else {
        sidebar.classList.toggle('collapsed');
    }
}

function logout() {
    Api.clearAuth();
    window.location.href = 'index.html';
}

// ═══════════════════════════════════════════════════════════════
//  DASHBOARD
// ═══════════════════════════════════════════════════════════════

async function loadDashboard() {
    try {
        const [summary, categories, trends, recent] = await Promise.all([
            Api.getDashboardSummary(),
            Api.getCategorySummary(),
            Api.getMonthlyTrends(),
            Api.getRecentActivity()
        ]);

        renderSummaryCards(summary);
        renderMonthlyChart(trends);
        renderCategoryChart(categories);
        renderRecentActivity(recent);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderSummaryCards(data) {
    document.getElementById('totalIncome').textContent = formatCurrency(data.totalIncome);
    document.getElementById('totalExpense').textContent = formatCurrency(data.totalExpense);
    document.getElementById('netBalance').textContent = formatCurrency(data.netBalance);
    document.getElementById('totalRecords').textContent = data.totalRecords;
}

function renderMonthlyChart(trends) {
    const ctx = document.getElementById('monthlyChart').getContext('2d');

    if (monthlyChartInstance) monthlyChartInstance.destroy();

    const labels = trends.map(t => t.monthName + ' ' + t.year);
    const incomeData = trends.map(t => t.totalIncome);
    const expenseData = trends.map(t => t.totalExpense);

    monthlyChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2,
                    pointRadius: 4,
                    pointBackgroundColor: '#10b981'
                },
                {
                    label: 'Expense',
                    data: expenseData,
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2,
                    pointRadius: 4,
                    pointBackgroundColor: '#ef4444'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: '#94a3b8', font: { family: 'Inter' } }
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255,255,255,0.04)' },
                    ticks: { color: '#64748b', font: { family: 'Inter', size: 11 } }
                },
                y: {
                    grid: { color: 'rgba(255,255,255,0.04)' },
                    ticks: {
                        color: '#64748b',
                        font: { family: 'Inter', size: 11 },
                        callback: v => '₹' + v.toLocaleString()
                    }
                }
            }
        }
    });
}

function renderCategoryChart(categories) {
    const ctx = document.getElementById('categoryChart').getContext('2d');

    if (categoryChartInstance) categoryChartInstance.destroy();

    if (!categories.length) {
        ctx.font = '14px Inter';
        ctx.fillStyle = '#64748b';
        ctx.textAlign = 'center';
        ctx.fillText('No data available', ctx.canvas.width / 2, ctx.canvas.height / 2);
        return;
    }

    const colors = ['#6366f1', '#10b981', '#ef4444', '#f59e0b', '#3b82f6', '#a855f7', '#ec4899', '#14b8a6'];

    categoryChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: categories.map(c => c.category),
            datasets: [{
                data: categories.map(c => c.totalAmount),
                backgroundColor: colors.slice(0, categories.length),
                borderWidth: 0,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Inter', size: 12 },
                        padding: 16,
                        usePointStyle: true,
                        pointStyleWidth: 8
                    }
                }
            }
        }
    });
}

function renderRecentActivity(records) {
    const container = document.getElementById('activityList');

    if (!records.length) {
        container.innerHTML = '<p style="text-align:center;color:var(--text-muted);padding:20px">No recent activity</p>';
        return;
    }

    container.innerHTML = records.map(r => `
        <div class="activity-item">
            <div class="activity-left">
                <div class="activity-icon ${r.type.toLowerCase()}">
                    <i class="fas fa-${r.type === 'INCOME' ? 'arrow-up' : 'arrow-down'}"></i>
                </div>
                <div>
                    <div class="activity-desc">${escapeHtml(r.description || r.category)}</div>
                    <div class="activity-cat">${escapeHtml(r.category)}</div>
                </div>
            </div>
            <div style="display:flex;align-items:center;gap:20px">
                <span class="activity-amount ${r.type.toLowerCase()}">${r.type === 'INCOME' ? '+' : '-'}${formatCurrency(r.amount)}</span>
                <span class="activity-date">${formatDate(r.date)}</span>
            </div>
        </div>
    `).join('');
}

// ═══════════════════════════════════════════════════════════════
//  RECORDS
// ═══════════════════════════════════════════════════════════════

async function loadRecords(page = 0) {
    currentPage = page;
    const role = currentUser.role;

    // Hide add/actions for non-admins
    if (role === 'ANALYST' || role === 'VIEWER') {
        document.getElementById('btnAddRecord').style.display = 'none';
        document.getElementById('thActions').style.display = 'none';
    }

    const params = {
        type: document.getElementById('filterType').value,
        category: document.getElementById('filterCategory').value,
        startDate: document.getElementById('filterStartDate').value,
        endDate: document.getElementById('filterEndDate').value,
        search: document.getElementById('filterSearch').value,
        page,
        size: 15
    };

    try {
        const data = await Api.getRecords(params);
        renderRecordsTable(data);
        renderPagination(data);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderRecordsTable(data) {
    const tbody = document.getElementById('recordsTableBody');
    const role = currentUser.role;
    const records = data.content || [];

    if (!records.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading-cell">No records found</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(r => `
        <tr>
            <td>${formatDate(r.date)}</td>
            <td><span class="badge badge-${r.type.toLowerCase()}">${r.type}</span></td>
            <td>${escapeHtml(r.category)}</td>
            <td class="amount-${r.type.toLowerCase()}">${r.type === 'INCOME' ? '+' : '-'}${formatCurrency(r.amount)}</td>
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escapeHtml(r.description || '—')}</td>
            <td>${escapeHtml(r.createdByName)}</td>
            ${role === 'ADMIN' ? `
                <td>
                    <button class="btn-icon" onclick="editRecord(${r.id})" title="Edit"><i class="fas fa-pen"></i></button>
                    <button class="btn-icon danger" onclick="confirmDeleteRecord(${r.id})" title="Delete"><i class="fas fa-trash"></i></button>
                </td>
            ` : (role === 'ANALYST' ? '' : `<td></td>`)}
        </tr>
    `).join('');
}

function renderPagination(data) {
    const container = document.getElementById('recordsPagination');
    const totalPages = data.totalPages || 0;
    const current = data.number || 0;

    if (totalPages <= 1) { container.innerHTML = ''; return; }

    let html = `<button onclick="loadRecords(${current - 1})" ${current === 0 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i>
    </button>`;

    for (let i = 0; i < totalPages; i++) {
        if (totalPages > 7 && Math.abs(i - current) > 2 && i !== 0 && i !== totalPages - 1) {
            if (i === current - 3 || i === current + 3) html += '<button disabled>...</button>';
            continue;
        }
        html += `<button class="${i === current ? 'active' : ''}" onclick="loadRecords(${i})">${i + 1}</button>`;
    }

    html += `<button onclick="loadRecords(${current + 1})" ${current >= totalPages - 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-right"></i>
    </button>`;

    container.innerHTML = html;
}

function debounceSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => loadRecords(), 400);
}

function openRecordModal(record = null) {
    document.getElementById('recordForm').reset();
    document.getElementById('recordId').value = '';
    document.getElementById('recordDate').value = new Date().toISOString().split('T')[0];

    if (record) {
        document.getElementById('recordModalTitle').innerHTML = '<i class="fas fa-pen-to-square"></i> Edit Record';
        document.getElementById('recordId').value = record.id;
        document.getElementById('recordAmount').value = record.amount;
        document.getElementById('recordType').value = record.type;
        document.getElementById('recordCategory').value = record.category;
        document.getElementById('recordDate').value = record.date;
        document.getElementById('recordDescription').value = record.description || '';
    } else {
        document.getElementById('recordModalTitle').innerHTML = '<i class="fas fa-plus-circle"></i> Add Record';
    }

    openModal('recordModal');
}

async function editRecord(id) {
    try {
        const record = await Api.getRecord(id);
        openRecordModal(record);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function submitRecord(e) {
    e.preventDefault();
    const id = document.getElementById('recordId').value;
    const data = {
        amount: parseFloat(document.getElementById('recordAmount').value),
        type: document.getElementById('recordType').value,
        category: document.getElementById('recordCategory').value,
        date: document.getElementById('recordDate').value,
        description: document.getElementById('recordDescription').value
    };

    try {
        if (id) {
            await Api.updateRecord(id, data);
            showToast('Record updated successfully', 'success');
        } else {
            await Api.createRecord(data);
            showToast('Record created successfully', 'success');
        }
        closeModal('recordModal');
        loadRecords(currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function confirmDeleteRecord(id) {
    if (confirm('Are you sure you want to delete this record?')) {
        deleteRecord(id);
    }
}

async function deleteRecord(id) {
    try {
        await Api.deleteRecord(id);
        showToast('Record deleted', 'success');
        loadRecords(currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ═══════════════════════════════════════════════════════════════
//  USERS
// ═══════════════════════════════════════════════════════════════

async function loadUsers() {
    try {
        const users = await Api.getUsers();
        renderUsersTable(users);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');

    if (!users.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading-cell">No users found</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(u => `
        <tr>
            <td>#${u.id}</td>
            <td style="font-weight:500;color:var(--text-primary)">${escapeHtml(u.name)}</td>
            <td>${escapeHtml(u.email)}</td>
            <td><span class="badge badge-${u.role.toLowerCase()}">${u.role}</span></td>
            <td><span class="badge badge-${u.status.toLowerCase()}">${u.status}</span></td>
            <td>${formatDateTime(u.createdAt)}</td>
            <td>
                <button class="btn-icon" onclick="editUser(${u.id})" title="Edit"><i class="fas fa-pen"></i></button>
                <button class="btn-icon" onclick="toggleUserStatus(${u.id}, '${u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}')" title="${u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}">
                    <i class="fas fa-${u.status === 'ACTIVE' ? 'ban' : 'check-circle'}" style="color:${u.status === 'ACTIVE' ? 'var(--amber)' : 'var(--green)'}"></i>
                </button>
                <button class="btn-icon danger" onclick="confirmDeleteUser(${u.id}, '${escapeHtml(u.name)}')" title="Delete"><i class="fas fa-trash"></i></button>
            </td>
        </tr>
    `).join('');
}

function openUserModal(user = null) {
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';

    if (user) {
        document.getElementById('userModalTitle').innerHTML = '<i class="fas fa-user-pen"></i> Edit User';
        document.getElementById('userId').value = user.id;
        document.getElementById('userFormName').value = user.name;
        document.getElementById('userFormEmail').value = user.email;
        document.getElementById('userFormRole').value = user.role;
        document.getElementById('userPasswordGroup').style.display = 'none';
        document.getElementById('userFormPassword').removeAttribute('required');
    } else {
        document.getElementById('userModalTitle').innerHTML = '<i class="fas fa-user-plus"></i> Add User';
        document.getElementById('userPasswordGroup').style.display = 'block';
        document.getElementById('userFormPassword').setAttribute('required', 'true');
    }

    openModal('userModal');
}

async function editUser(id) {
    try {
        const user = await Api.getUser(id);
        openUserModal(user);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function submitUser(e) {
    e.preventDefault();
    const id = document.getElementById('userId').value;

    if (id) {
        // Update
        const data = {
            name: document.getElementById('userFormName').value,
            email: document.getElementById('userFormEmail').value,
            role: document.getElementById('userFormRole').value
        };
        try {
            await Api.updateUser(id, data);
            showToast('User updated successfully', 'success');
            closeModal('userModal');
            loadUsers();
        } catch (err) {
            showToast(err.message, 'error');
        }
    } else {
        // Create
        const data = {
            name: document.getElementById('userFormName').value,
            email: document.getElementById('userFormEmail').value,
            password: document.getElementById('userFormPassword').value,
            role: document.getElementById('userFormRole').value
        };
        try {
            await Api.createUser(data);
            showToast('User created successfully', 'success');
            closeModal('userModal');
            loadUsers();
        } catch (err) {
            showToast(err.message, 'error');
        }
    }
}

async function toggleUserStatus(id, newStatus) {
    try {
        await Api.updateUserStatus(id, newStatus);
        showToast(`User ${newStatus.toLowerCase()}d`, 'success');
        loadUsers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function confirmDeleteUser(id, name) {
    if (confirm(`Are you sure you want to delete user "${name}"?`)) {
        deleteUser(id);
    }
}

async function deleteUser(id) {
    try {
        await Api.deleteUser(id);
        showToast('User deleted', 'success');
        loadUsers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ═══════════════════════════════════════════════════════════════
//  UTILITIES
// ═══════════════════════════════════════════════════════════════

function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i> ${escapeHtml(message)}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(40px)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function formatCurrency(amount) {
    return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatDateTime(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
