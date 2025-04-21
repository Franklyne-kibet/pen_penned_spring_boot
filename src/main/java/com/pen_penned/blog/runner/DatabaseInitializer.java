package com.pen_penned.blog.runner;

import com.pen_penned.blog.model.AppRole;
import com.pen_penned.blog.model.Role;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.RoleRepository;
import com.pen_penned.blog.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Create roles first if they don't exist
        Role userRole = ensureRoleExists(AppRole.ROLE_USER);
        Role editorRole = ensureRoleExists(AppRole.ROLE_EDITOR);
        Role adminRole = ensureRoleExists(AppRole.ROLE_ADMIN);

        // Create users with roles
        ensureUserExists("John", "Doe", "john@example.com", "password1", userRole);
        ensureUserExists("Mary", "Johns", "mary@example.com", "password2", userRole);
        ensureUserExists("Walter", "Kibet", "walter@example.com", "adminPass", userRole);
    }

    @Transactional
    private Role ensureRoleExists(AppRole appRole) {
        return roleRepository.findByRoleName(appRole)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(appRole);
                    return roleRepository.save(newRole);
                });
    }

    private void ensureUserExists(String firstName, String lastName, String email, String password, Role... roles) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.createUser(firstName, lastName, email, passwordEncoder.encode(password));
            for (Role role : roles) {
                user.addRole(role);
            }
            userRepository.save(user);
        }
    }
}
