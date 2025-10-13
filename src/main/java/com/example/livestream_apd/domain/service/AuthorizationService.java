package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.domain.entity.Permission;
import com.example.livestream_apd.domain.entity.Role;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.PermissionRepository;
import com.example.livestream_apd.domain.repository.RoleRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional //

public class AuthorizationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public void assignRoleToUser(Long userId, Role.RoleName  roleName) {
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(roleName).orElseThrow(()-> new RuntimeException("Role not found"));
        user.addRole(role);
        userRepository.save(user);
        log.info("Assigned role {} to user: {}", role, user.getEmail());
    }

    public void removeRoleFromUser(Long userId, Role.RoleName  roleName) {
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(roleName).orElseThrow(()-> new RuntimeException("Role not found"));
        user.removeRole(role);
        userRepository.save(user);
        log.info("Removed role {} from user: {}", role, user.getEmail());
    }

    public boolean hasPermission(Long userId, String permissionName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()){
            return false;
        }
        return userOptional.get().hasPermission(permissionName);
    }

    public boolean hasRole(Long userId, Role.RoleName roleName) {
        Optional<User> roleOptional = userRepository.findById(userId);
        if(roleOptional.isEmpty()){
            return false;
        }
        return roleOptional.get().hasRole(roleName);
    }

    public Set<String> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<Role.RoleName> getUserRoles(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRoles().stream()
                            .map(Role :: getName)
                            .collect(java.util.stream.Collectors.toSet());
    }

    public boolean canManageUser(Long currentUserId, Long targetUserId) {
        if(currentUserId.equals(targetUserId)){
            return true;
        }
        return hasRole(currentUserId, Role.RoleName.ADMIN);
    }

    public boolean canAccessResource(Long userId, String resource, String action){
        String permissionName = resource.toUpperCase() + "_" + action.toUpperCase();
        return hasPermission(userId, permissionName);

    }

}
