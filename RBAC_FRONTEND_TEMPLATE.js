/**
 * RBAC Frontend Template
 * Copy and paste this code into each HTML page that needs RBAC protection
 */

// 1. Add data-role-access attributes to sidebar menu items:
//    - Resident pages: data-role-access="resident"
//    - Financial pages: data-role-access="financial"
//    - Admin only: data-role-access="admin"

// 2. Add this function to the page (before closing </script> tag):
function applyRBAC() {
    // Hide/Show sidebar menu items based on role
    $('[data-role-access="financial"]').each(function() {
        if (!APIUtils.canAccessFinancial()) {
            $(this).hide();
        }
    });

    $('[data-role-access="resident"]').each(function() {
        if (!APIUtils.canAccessResident()) {
            $(this).hide();
        }
    });
}

// 3. Add redirect logic in document.ready (for Resident pages):
$(document).ready(function() {
    // Check access and redirect if needed
    if (!APIUtils.canAccessResident()) {
        alert('Bạn không có quyền truy cập trang này!');
        window.location.href = 'index.html';
        return;
    }
    
    // Apply RBAC: Hide/Show menu items
    applyRBAC();
    
    // ... rest of your code
});

// 3b. For Financial pages, use:
$(document).ready(function() {
    // Check access and redirect if needed
    if (!APIUtils.canAccessFinancial()) {
        alert('Bạn không có quyền truy cập trang này!');
        window.location.href = 'index.html';
        return;
    }
    
    // Apply RBAC: Hide/Show menu items
    applyRBAC();
    
    // ... rest of your code
});

