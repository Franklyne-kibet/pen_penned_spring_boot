package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.LocalUserRequestDTO;
import com.pen_penned.blog.dto.request.OAuth2UserRequestDTO;
import com.pen_penned.blog.model.AppRole;
import com.pen_penned.blog.model.AuthProvider;
import com.pen_penned.blog.model.Role;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.RoleRepository;
import com.pen_penned.blog.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public User createLocalUser(LocalUserRequestDTO localUserRequestDTO) {
        // Map DTO to entity
        User user = modelMapper.map(localUserRequestDTO, User.class);

        // Encode password
        user.setPassword(passwordEncoder.encode(localUserRequestDTO.getPassword()));
        user.setProvider(AuthProvider.LOCAL);

        // Set default USER role
        addUserRole(user);

        return userRepository.save(user);
    }

    @Transactional
    public User createOAuth2User(
            OAuth2UserRequestDTO oauth2UserRequestDTO,
            AuthProvider provider,
            String providerId) {
        // Map DTO to entity
        User user = modelMapper.map(oauth2UserRequestDTO, User.class);

        // Set OAuth provider details
        user.setProvider(provider);
        user.setProviderId(providerId);
        // No password required for OAuth2 users

        // Set default USER role
        addUserRole(user);

        return userRepository.save(user);
    }

    @Transactional
    public User updateOAuth2User(User existingUser, OAuth2UserRequestDTO oauth2UserRequestDTO) {
        existingUser.setFirstName(oauth2UserRequestDTO.getFirstName());
        existingUser.setLastName(oauth2UserRequestDTO.getLastName());

        return userRepository.save(existingUser);
    }


    private void addUserRole(User user) {
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
    }
}
