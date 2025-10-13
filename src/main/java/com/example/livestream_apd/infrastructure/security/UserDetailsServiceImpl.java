package com.example.livestream_apd.infrastructure.security;


import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }

    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String email;
        private String username;
        private String password;
        private String fullName;
        private User.UserStatus status;
        private Boolean isEmailVerified;
        private Collection<? extends GrantedAuthority> authorities;

        public UserPrincipal(Long id, String email, String username, String password, String fullName,
                             User.UserStatus status, Boolean isEmailVerified, Collection<? extends GrantedAuthority> authorities) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.status = status;
            this.isEmailVerified = isEmailVerified;
            this.authorities = authorities;
        }
        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Add roles and permissions from user's roles
            if (user.getRoles() != null) {
                user.getRoles().forEach(role -> {
                    // Add role authority
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));

                    // Add permission authorities
                    if (role.getPermissions() != null) {
                        role.getPermissions().forEach(permission ->
                                authorities.add(new SimpleGrantedAuthority(permission.getName()))
                        );
                    }
                });
            }

            // Add default role if no roles assigned
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // Add verified user role if email is verified
            if (user.getIsEmailVerified()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_VERIFIED_USER"));
            }

            return new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getFullName(),
                    user.getStatus(),
                    user.getIsEmailVerified(),
                    authorities
            );
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        @Override
        public String getUsername() {
            return email; // Use email as username for authentication
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return status != User.UserStatus.BANNED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return status == User.UserStatus.ACTIVE ||
                    (status == User.UserStatus.PENDING_VERIFICATION && isEmailVerified);
        }
    }
}