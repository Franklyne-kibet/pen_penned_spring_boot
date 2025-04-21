package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.LocalUserDTO;
import com.pen_penned.blog.dto.request.OAuth2UserDTO;
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

    
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public User createLocalUser(LocalUserDTO localUserDTO) {
        // Map DTO to entity
        User user = modelMapper.map(localUserDTO, User.class);

        // Encode password
        user.setPassword(passwordEncoder.encode(localUserDTO.getPassword()));
        user.setProvider(AuthProvider.LOCAL);

        // Set default USER role
        addUserRole(user);

        return userRepository.save(user);
    }

    @Transactional
    public User createOAuth2User(OAuth2UserDTO oauth2UserDTO, AuthProvider provider, String providerId) {
        // Map DTO to entity
        User user = modelMapper.map(oauth2UserDTO, User.class);

        // Set OAuth provider details
        user.setProvider(provider);
        user.setProviderId(providerId);
        // No password required for OAuth2 users

        // Set default USER role
        addUserRole(user);

        return userRepository.save(user);
    }

    @Transactional
    public User updateOAuth2User(User existingUser, OAuth2UserDTO oauth2UserDTO) {
        existingUser.setFirstName(oauth2UserDTO.getFirstName());
        existingUser.setLastName(oauth2UserDTO.getLastName());

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
