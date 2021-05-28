package com.dusk.shared.usersManagement.models;

import com.dusk.shared.commons.models.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private Status status;

    public UserDetailsImpl(Long id, String username, String email, String password, Status status,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.status = status;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEncrypted_password(),
                user.getStatus(),
                authorities);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !status.getName().equals("USER_SELF_SUSPENDED") &&
               !status.getName().equals("USER_SUSPENDED_BY_SUPERADMIN");
    }

    @Override
    public boolean isAccountNonLocked() {
        return status.getName().equals("USER_ACTIVATED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !status.getName().equals("USER_SELF_SUSPENDED") &&
               !status.getName().equals("USER_SUSPENDED_BY_SUPERADMIN");
    }

    @Override
    public boolean isEnabled() {
        return status.getName().equals("USER_ACTIVATED") ? true : false;
    }

    public String getEmail() {
        return email;
    }
}
