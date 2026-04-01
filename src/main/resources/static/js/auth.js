/* ═══════════════════════════════════════════════════════════════
   Auth Page Logic
   ═══════════════════════════════════════════════════════════════ */

// Redirect if already logged in
if (Api.isLoggedIn()) {
    window.location.href = 'dashboard.html';
}

// Toggle between login and register forms
function toggleAuth(e) {
    e.preventDefault();
    document.getElementById('loginForm').classList.toggle('active');
    document.getElementById('registerForm').classList.toggle('active');
    document.getElementById('authError').style.display = 'none';
}

// Toggle password visibility
function togglePassword(inputId, btn) {
    const input = document.getElementById(inputId);
    const icon = btn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// Fill demo credentials
function fillCredentials(email, password) {
    document.getElementById('loginEmail').value = email;
    document.getElementById('loginPassword').value = password;
    // Make sure login form is visible
    document.getElementById('loginForm').classList.add('active');
    document.getElementById('registerForm').classList.remove('active');
}

// Show auth error
function showAuthError(message) {
    const el = document.getElementById('authError');
    el.textContent = message;
    el.style.display = 'block';
}

// Login form submit
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const btn = e.target.querySelector('button[type=submit]');

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing in...';

    try {
        const data = await Api.login(email, password);
        Api.setAuth(data);
        window.location.href = 'dashboard.html';
    } catch (err) {
        showAuthError(err.message);
        btn.disabled = false;
        btn.innerHTML = '<span>Sign In</span><i class="fas fa-arrow-right"></i>';
    }
});

// Register form submit
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const btn = e.target.querySelector('button[type=submit]');

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';

    try {
        const data = await Api.register(name, email, password);
        Api.setAuth(data);
        window.location.href = 'dashboard.html';
    } catch (err) {
        showAuthError(err.message);
        btn.disabled = false;
        btn.innerHTML = '<span>Create Account</span><i class="fas fa-user-plus"></i>';
    }
});
