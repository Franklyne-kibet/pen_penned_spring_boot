package com.pen_penned.blog.security.oauth2;

import com.pen_penned.blog.exception.OAuth2AuthenticationProcessingException;
import com.pen_penned.blog.model.AppRole;
import com.pen_penned.blog.model.AuthProvider;
import com.pen_penned.blog.model.Role;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.RoleRepository;
import com.pen_penned.blog.repositories.UserRepository;
import com.pen_penned.blog.security.oauth2.user.OAuth2UserInfo;
import com.pen_penned.blog.security.oauth2.user.OAuth2UserInfoFactory;
import com.pen_penned.blog.security.services.UserDetailsImpl;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User)
            throws OAuth2AuthenticationProcessingException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider;

        if ("google".equals(registrationId)) {
            provider = AuthProvider.GOOGLE;
        } else if ("github".equals(registrationId)) {
            provider = AuthProvider.GITHUB;
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with "
                    + registrationId + " is not supported yet.");
        }

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            if (!user.getProvider().equals(provider)) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }

            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo, provider);
        }

        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest,
                                 OAuth2UserInfo oAuth2UserInfo, AuthProvider provider) {
        User user = new User();

        user.setProvider(provider);
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());

        // Handle name splitting
        String fullName = StringUtils.hasText(oAuth2UserInfo.getName())
                ? oAuth2UserInfo.getName()
                : oAuth2UserInfo.getEmail().split("@")[0];

        String[] nameParts = fullName.trim().split(" ", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        // Set default USER role
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        String fullName = StringUtils.hasText(oAuth2UserInfo.getName())
                ? oAuth2UserInfo.getName()
                : oAuth2UserInfo.getEmail().split("@")[0];

        String[] nameParts = fullName.trim().split(" ", 2);
        existingUser.setFirstName(nameParts[0]);
        existingUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        return userRepository.save(existingUser);
    }

}
