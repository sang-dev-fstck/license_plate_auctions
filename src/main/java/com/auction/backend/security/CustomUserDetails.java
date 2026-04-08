package com.auction.backend.security;

import com.auction.backend.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    // Đánh dấu transient (tùy chọn) để báo cho máy ảo Java biết không cần serialize toàn bộ cục Account này nếu có ném qua mạng
    private final transient Account account;
    
    // Cấp quyền cho User (Admin hay User thường)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail(); // Dùng Email làm tài khoản đăng nhập
    }

    // Các hàm dưới đây tạm thời cho return true (Tài khoản luôn hoạt động)
    // Sau này bạn có thể map field `isActive` của entity User vào đây.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getIsActive() != null ? account.getIsActive() : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.getIsActive() != null ? account.getIsActive() : true;
    }
}
