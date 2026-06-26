<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String pageTitle = "Change Password";
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-key me-2 text-danger"></i>
                Change Password
            </h4>
            <small class="text-muted">
                Update your account password securely
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/faculty/profile"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Back to Profile
        </a>
    </div>

    <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
    <div class="alert alert-danger d-flex gap-2" role="alert" style="border-radius:12px;">
        <i class="fas fa-exclamation-circle"></i>
        <div><%= errorMessage %></div>
    </div>
    <% } %>

    <div class="row justify-content-center">
        <div class="col-lg-5">
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
                <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                    <h6 class="fw-bold text-dark mb-0">
                        <i class="fas fa-lock me-2 text-danger"></i>
                        Password Update
                    </h6>
                    <p class="text-muted small mt-1 mb-0">
                        Minimum 6 characters. Choose a strong password.
                    </p>
                </div>
                <div class="card-body p-4">
                    <form method="POST"
                          action="<%= request.getContextPath() %>/faculty/change-password"
                          id="changePwdForm" novalidate>

                        <!-- Current Password -->
                        <div class="mb-3">
                            <label for="oldPassword" class="form-label fw-medium small">
                                <i class="fas fa-unlock me-1 text-muted"></i>
                                Current Password <span class="text-danger">*</span>
                            </label>
                            <div class="input-group">
                                <input type="password" id="oldPassword" name="oldPassword"
                                       class="form-control"
                                       placeholder="Enter current password"
                                       style="border-radius:10px 0 0 10px;" required>
                                <button class="btn btn-outline-secondary"
                                        type="button"
                                        onclick="togglePwd('oldPassword', 'eye1')"
                                        style="border-radius:0 10px 10px 0;">
                                    <i class="fas fa-eye" id="eye1"></i>
                                </button>
                            </div>
                        </div>

                        <!-- New Password -->
                        <div class="mb-3">
                            <label for="newPassword" class="form-label fw-medium small">
                                <i class="fas fa-lock me-1 text-muted"></i>
                                New Password <span class="text-danger">*</span>
                            </label>
                            <div class="input-group">
                                <input type="password" id="newPassword" name="newPassword"
                                       class="form-control"
                                       placeholder="Enter new password (min 6 chars)"
                                       style="border-radius:10px 0 0 10px;"
                                       minlength="6" required>
                                <button class="btn btn-outline-secondary"
                                        type="button"
                                        onclick="togglePwd('newPassword', 'eye2')"
                                        style="border-radius:0 10px 10px 0;">
                                    <i class="fas fa-eye" id="eye2"></i>
                                </button>
                            </div>
                            <!-- Strength bar -->
                            <div class="progress mt-1" style="height:5px;">
                                <div class="progress-bar" id="strengthBar"
                                     style="width:0%;transition:width 0.3s;"></div>
                            </div>
                            <div class="form-text" id="strengthText"></div>
                        </div>

                        <!-- Confirm New Password -->
                        <div class="mb-4">
                            <label for="confirmPassword" class="form-label fw-medium small">
                                <i class="fas fa-check-double me-1 text-muted"></i>
                                Confirm New Password <span class="text-danger">*</span>
                            </label>
                            <div class="input-group">
                                <input type="password" id="confirmPassword"
                                       name="confirmPassword"
                                       class="form-control"
                                       placeholder="Re-enter new password"
                                       style="border-radius:10px 0 0 10px;" required>
                                <button class="btn btn-outline-secondary"
                                        type="button"
                                        onclick="togglePwd('confirmPassword', 'eye3')"
                                        style="border-radius:0 10px 10px 0;">
                                    <i class="fas fa-eye" id="eye3"></i>
                                </button>
                            </div>
                            <div id="matchMsg" class="form-text"></div>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit"
                                    class="btn btn-danger rounded-pill px-4 fw-semibold">
                                <i class="fas fa-key me-2"></i>Change Password
                            </button>
                            <a href="<%= request.getContextPath() %>/faculty/profile"
                               class="btn btn-outline-secondary rounded-pill px-4">
                                Cancel
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tips Card -->
            <div class="card border-0 shadow-sm mt-3 p-3" style="border-radius:14px;">
                <h6 class="fw-semibold small text-muted mb-2">
                    <i class="fas fa-shield-alt me-1"></i> Password Tips
                </h6>
                <ul class="mb-0 ps-3" style="font-size:0.82rem;color:#6c757d;">
                    <li>Use at least 6 characters</li>
                    <li>Mix uppercase, lowercase, numbers &amp; symbols</li>
                    <li>Avoid using your name or email</li>
                    <li>Never share your password</li>
                </ul>
            </div>
        </div>
    </div>
</main>

<script>
    // ── Toggle Password Visibility ─────────────────────────────────
    function togglePwd(inputId, iconId) {
        const inp  = document.getElementById(inputId);
        const icon = document.getElementById(iconId);
        if (inp.type === 'password') {
            inp.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            inp.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    }

    // ── Password Strength Meter ────────────────────────────────────
    document.getElementById('newPassword').addEventListener('input', function () {
        const val  = this.value;
        const bar  = document.getElementById('strengthBar');
        const text = document.getElementById('strengthText');

        let score = 0;
        if (val.length >= 6)                      score++;
        if (val.length >= 10)                     score++;
        if (/[A-Z]/.test(val))                    score++;
        if (/[0-9]/.test(val))                    score++;
        if (/[@#$!%*?&^()_\-+=]/.test(val))       score++;

        const levels = [
            { pct: 0,   cls: '',        label: ''         },
            { pct: 20,  cls: 'bg-danger',  label: 'Very Weak' },
            { pct: 40,  cls: 'bg-danger',  label: 'Weak'      },
            { pct: 60,  cls: 'bg-warning', label: 'Fair'      },
            { pct: 80,  cls: 'bg-info',    label: 'Strong'    },
            { pct: 100, cls: 'bg-success', label: 'Very Strong'}
        ];
        const l = levels[score] || levels[0];
        bar.style.width    = l.pct + '%';
        bar.className      = 'progress-bar ' + l.cls;
        text.textContent   = l.label;
        text.className     = 'form-text ' +
            (score >= 4 ? 'text-success' : score >= 2 ? 'text-warning' : 'text-danger');
    });

    // ── Confirm Password Match ─────────────────────────────────────
    document.getElementById('confirmPassword').addEventListener('input', function () {
        const np  = document.getElementById('newPassword').value;
        const msg = document.getElementById('matchMsg');
        if (this.value === np && np.length > 0) {
            msg.textContent = '✔ Passwords match';
            msg.className   = 'form-text text-success';
        } else if (this.value.length > 0) {
            msg.textContent = '✘ Passwords do not match';
            msg.className   = 'form-text text-danger';
        } else {
            msg.textContent = '';
        }
    });

    // ── Client-side validation before submit ──────────────────────
    document.getElementById('changePwdForm').addEventListener('submit', function (e) {
        const np  = document.getElementById('newPassword').value;
        const cp  = document.getElementById('confirmPassword').value;
        const old = document.getElementById('oldPassword').value;

        if (!old || !np || !cp) {
            e.preventDefault();
            alert('All fields are required.');
            return;
        }
        if (np !== cp) {
            e.preventDefault();
            alert('New password and confirm password do not match.');
            return;
        }
        if (np.length < 6) {
            e.preventDefault();
            alert('New password must be at least 6 characters long.');
        }
    });
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>