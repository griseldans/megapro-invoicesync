package com.megapro.invoicesync.security;

import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.megapro.invoicesync.model.Employee;
import com.megapro.invoicesync.model.UserApp;
import com.megapro.invoicesync.repository.UserAppDb;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserAppDb userDb;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserApp user = userDb.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (user instanceof Employee && ((Employee) user).isDeleted()) {
            throw new UsernameNotFoundException("User is deleted");
        }
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        grantedAuthoritySet.add(new SimpleGrantedAuthority(user.getRole().getRole()));

        return new CustomUserDetails(user, grantedAuthoritySet);
    }

    
}
