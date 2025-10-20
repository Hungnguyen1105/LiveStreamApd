package com.example.livestream_apd.config;


import com.example.livestream_apd.domain.entity.Permission;
import com.example.livestream_apd.domain.entity.Role;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.PermissionRepository;
import com.example.livestream_apd.domain.repository.RoleRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing default roles and permissions...");

        createPermissions();
        createRoles();
        createDefaultAdmin();

        log.info("Default roles and permissions initialized successfully");
    }

    private void createPermissions() {
        createPermissionIfNotExists(Permission.USER_READ, "Read user information", "USER");
        createPermissionIfNotExists(Permission.USER_WRITE, "Edit user information", "USER");
        createPermissionIfNotExists(Permission.USER_DELETE, "Delete user account", "USER");
        createPermissionIfNotExists(Permission.USER_MANAGE, "Manage users", "USER");

        createPermissionIfNotExists(Permission.ADMIN_READ, "Read admin information", "ADMIN");
        createPermissionIfNotExists(Permission.ADMIN_WRITE, "Edit admin settings", "ADMIN");
        createPermissionIfNotExists(Permission.ADMIN_DELETE, "Delete admin data", "ADMIN");
        createPermissionIfNotExists(Permission.ADMIN_MANAGE, "Manage admin functions", "ADMIN");

        createPermissionIfNotExists(Permission.CONTENT_CREATE, "Create content", "CONTENT");
        createPermissionIfNotExists(Permission.CONTENT_EDIT, "Edit content", "CONTENT");
        createPermissionIfNotExists(Permission.CONTENT_DELETE, "Delete content", "CONTENT");
        createPermissionIfNotExists(Permission.CONTENT_MODERATE, "Moderate content", "CONTENT");

        createPermissionIfNotExists(Permission.PAYMENT_READ, "View payment information", "PAYMENT");
        createPermissionIfNotExists(Permission.PAYMENT_WRITE, "Process payments", "PAYMENT");
        createPermissionIfNotExists(Permission.PAYMENT_MANAGE, "Manage payment system", "PAYMENT");

        createPermissionIfNotExists(Permission.STREAM_CREATE, "Create streams", "STREAM");
        createPermissionIfNotExists(Permission.STREAM_MANAGE, "Manage streams", "STREAM");
        createPermissionIfNotExists(Permission.STREAM_MODERATE, "Moderate streams", "STREAM");

        createPermissionIfNotExists(Permission.REPORT_VIEW, "View reports", "REPORT");
        createPermissionIfNotExists(Permission.REPORT_MANAGE, "Manage reports", "REPORT");
    }
    private void createRoles() {
        // USER role
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(findPermission(Permission.USER_READ));
        userPermissions.add(findPermission(Permission.USER_WRITE));
        userPermissions.add(findPermission(Permission.CONTENT_CREATE));
        createRoleIfNotExists(Role.RoleName.USER, "Regular user", userPermissions);

        // STREAMER role
        Set<Permission> streamerPermissions = new HashSet<>();
        streamerPermissions.add(findPermission(Permission.USER_READ));
        streamerPermissions.add(findPermission(Permission.USER_WRITE));
        streamerPermissions.add(findPermission(Permission.CONTENT_CREATE));
        streamerPermissions.add(findPermission(Permission.CONTENT_EDIT));
        streamerPermissions.add(findPermission(Permission.STREAM_CREATE));
        streamerPermissions.add(findPermission(Permission.STREAM_MANAGE));
        createRoleIfNotExists(Role.RoleName.STREAMER, "Content streamer", streamerPermissions);

        // VIP role
        Set<Permission> vipPermissions = new HashSet<>();
        vipPermissions.add(findPermission(Permission.USER_READ));
        vipPermissions.add(findPermission(Permission.USER_WRITE));
        vipPermissions.add(findPermission(Permission.CONTENT_CREATE));
        vipPermissions.add(findPermission(Permission.CONTENT_EDIT));
        vipPermissions.add(findPermission(Permission.PAYMENT_READ));
        vipPermissions.add(findPermission(Permission.STREAM_CREATE));
        createRoleIfNotExists(Role.RoleName.VIP, "VIP user", vipPermissions);

        // MODERATOR role
        Set<Permission> moderatorPermissions = new HashSet<>();
        moderatorPermissions.add(findPermission(Permission.USER_READ));
        moderatorPermissions.add(findPermission(Permission.USER_WRITE));
        moderatorPermissions.add(findPermission(Permission.USER_MANAGE));
        moderatorPermissions.add(findPermission(Permission.CONTENT_CREATE));
        moderatorPermissions.add(findPermission(Permission.CONTENT_EDIT));
        moderatorPermissions.add(findPermission(Permission.CONTENT_DELETE));
        moderatorPermissions.add(findPermission(Permission.CONTENT_MODERATE));
        moderatorPermissions.add(findPermission(Permission.STREAM_MODERATE));
        moderatorPermissions.add(findPermission(Permission.REPORT_VIEW));
        moderatorPermissions.add(findPermission(Permission.REPORT_MANAGE));
        createRoleIfNotExists(Role.RoleName.MODERATOR, "Content moderator", moderatorPermissions);

        // ADMIN role (all permissions)
        Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
        createRoleIfNotExists(Role.RoleName.ADMIN, "System administrator", adminPermissions);
    }

    private void createPermissionIfNotExists(String name, String description, String category) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = Permission.builder()
                    .name(name)
                    .description(description)
                    .category(category)
                    .build();
            permissionRepository.save(permission);
            log.debug("Created permission: {}", name);
        }
    }

    private void createRoleIfNotExists(Role.RoleName roleName, String description, Set<Permission> permissions) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
            log.debug("Created role: {}", roleName);
        }
    }

    private Permission findPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    private void createDefaultAdmin() {
        // Check if admin already exists
        if (userRepository.existsByEmail("admin@livehungbao.com")) {
            log.info("Default admin user already exists");
            return;
        }

        // Create admin user
        User adminUser = User.builder()
                .username("admin")
                .email("admin@livehungbao.com")
                .passwordHash(passwordEncoder.encode("Admin123@#")) // Default password
                .fullName("System Administrator")
                .bio("Default system administrator account")
                .status(User.UserStatus.ACTIVE)
                .isEmailVerified(true)
                .isVerified(true)
                .build();

        // Assign ADMIN role
        Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        adminUser.addRole(adminRole);

        userRepository.save(adminUser);

        log.info("Default admin user created: admin@livehungbao.com / Admin123#@");
    }
}
