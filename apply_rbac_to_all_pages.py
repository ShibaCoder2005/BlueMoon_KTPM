#!/usr/bin/env python3
"""
Script to apply RBAC to all HTML pages
"""
import re
import os

# Define pages and their access types
RESIDENT_PAGES = ['can-ho.html', 'danh-sach-dan-cu.html', 'phuong-tien.html']
FINANCIAL_PAGES = ['khoan-thu.html', 'dot-thu.html', 'phieu-thu.html', 'bao-cao.html', 'bao-cao-thu.html', 'bao-cao-cong-no.html']

def update_sidebar_menu_items(content):
    """Add data-role-access attributes to menu items"""
    # Resident menu items
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="can-ho\.html")',
        r'<li class="nav-item" data-role-access="resident">\n                <a class="nav-link" href="can-ho.html"',
        content
    )
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="danh-sach-dan-cu\.html")',
        r'<li class="nav-item" data-role-access="resident">\n                <a class="nav-link" href="danh-sach-dan-cu.html"',
        content
    )
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="phuong-tien\.html")',
        r'<li class="nav-item" data-role-access="resident">\n                <a class="nav-link" href="phuong-tien.html"',
        content
    )
    
    # Financial menu items
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="khoan-thu\.html")',
        r'<li class="nav-item" data-role-access="financial">\n                <a class="nav-link" href="khoan-thu.html"',
        content
    )
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="dot-thu\.html")',
        r'<li class="nav-item" data-role-access="financial">\n                <a class="nav-link" href="dot-thu.html"',
        content
    )
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="phieu-thu\.html")',
        r'<li class="nav-item" data-role-access="financial">\n                <a class="nav-link" href="phieu-thu.html"',
        content
    )
    content = re.sub(
        r'(<li class="nav-item[^"]*">\s*<a class="nav-link" href="bao-cao\.html")',
        r'<li class="nav-item" data-role-access="financial">\n                <a class="nav-link" href="bao-cao.html"',
        content
    )
    
    return content

def add_rbac_function(content):
    """Add applyRBAC function before closing script tag"""
    if 'function applyRBAC()' in content:
        return content  # Already has the function
    
    # Find the last </script> before </body>
    pattern = r'(</script>\s*</body>)'
    replacement = r'''        /**
         * Apply Role-Based Access Control (RBAC) to UI elements
         */
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
    </script>
</body>'''
    
    content = re.sub(pattern, replacement, content, count=1)
    return content

def add_rbac_check(content, access_type):
    """Add RBAC check in document.ready"""
    if 'APIUtils.canAccess' in content:
        return content  # Already has RBAC check
    
    # Find first $(document).ready
    pattern = r'(\$\(document\)\.ready\([^)]*function[^)]*\)\s*\{)'
    
    if access_type == 'resident':
        check_code = '''            // Apply RBAC: Check access and redirect if needed
            if (!APIUtils.canAccessResident()) {
                alert('Bạn không có quyền truy cập trang này!');
                window.location.href = 'index.html';
                return;
            }
            
            // Apply RBAC: Hide/Show menu items
            applyRBAC();
            
'''
    elif access_type == 'financial':
        check_code = '''            // Apply RBAC: Check access and redirect if needed
            if (!APIUtils.canAccessFinancial()) {
                alert('Bạn không có quyền truy cập trang này!');
                window.location.href = 'index.html';
                return;
            }
            
            // Apply RBAC: Hide/Show menu items
            applyRBAC();
            
'''
    else:
        return content
    
    replacement = r'\1\n' + check_code
    content = re.sub(pattern, replacement, content, count=1)
    return content

def process_file(filepath, access_type):
    """Process a single HTML file"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Update sidebar menu
        content = update_sidebar_menu_items(content)
        
        # Add RBAC function
        content = add_rbac_function(content)
        
        # Add RBAC check
        content = add_rbac_check(content, access_type)
        
        # Write back
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Updated {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error processing {filepath}: {e}")
        return False

def main():
    base_dir = 'src/main/resources'
    
    # Process resident pages
    for page in RESIDENT_PAGES:
        filepath = os.path.join(base_dir, page)
        if os.path.exists(filepath):
            process_file(filepath, 'resident')
    
    # Process financial pages
    for page in FINANCIAL_PAGES:
        filepath = os.path.join(base_dir, page)
        if os.path.exists(filepath):
            process_file(filepath, 'financial')
    
    print("\nDone! Please review the changes and test the pages.")

if __name__ == '__main__':
    main()

