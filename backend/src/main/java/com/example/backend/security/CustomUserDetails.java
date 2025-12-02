package com.example.backend.security;

import com.example.backend.entity.user.enumeration.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public class CustomUserDetails implements UserDetails {

    private final com.example.backend.entity.user.User user;

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if(user.getRole() != null ) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            if(user.getRole().equals(Role.ROLE_ECONOMIC)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ECONOMIC"));
            } else if(user.getRole().equals(Role.ROLE_PREMIER)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ECONOMIC"));
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIER"));
            } else if(user.getRole().equals(Role.ROLE_ADMIN)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ECONOMIC"));
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
