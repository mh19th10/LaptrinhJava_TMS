package vn.edu.uth.quanlidaythem.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.core.userdetails.UserDetails;

import vn.edu.uth.quanlidaythem.domain.User;

public class UserDetailsImpl implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final String fullName;

    public UserDetailsImpl(Long id, String username, String fullName, String password, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = RoleUtils.normalize(role);
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
            user.getId(), 
            user.getUsername(), 
            user.getFullName(),
            user.getPassword(), 
            user.getRole()
        );
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authorityString = "ROLE_" + role;

        System.out.println("\n\n!!! [DEBUG-2] Đang tạo quyền cho Spring Security: '" + authorityString + "'\n\n");
        
        // ✅ SỬA LỖI: Sử dụng SimpleGrantedAuthority, cách chuẩn nhất
        return Collections.singletonList(new SimpleGrantedAuthority(authorityString));
    }
    
    @Override 
    public String getPassword() { return password; }
    
    @Override 
    public String getUsername() { return username; }
    
    @Override 
    public boolean isAccountNonExpired() { return true; }
    
    @Override 
    public boolean isAccountNonLocked() { return true; }
    
    @Override 
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override 
    public boolean isEnabled() { return true; }
}
