package com.megapro.invoicesync.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;

public class CustomUserDetails implements UserDetails{
    private UserApp user;
    private Set<GrantedAuthority> authorities;

    public CustomUserDetails(UserApp user, Set<GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        if (user instanceof Employee) {
            return !((Employee) user).isDeleted();
        }
        return true;
    }
}
