package com.pen_penned.blog.security.oauth2;


import com.pen_penned.blog.dto.request.OAuth2UserRequestDTO;
import com.pen_penned.blog.exception.OAuth2AuthenticationProcessingException;
import com.pen_penned.blog.model.AuthProvider;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.UserRepository;
import com.pen_penned.blog.security.oauth2.user.OAuth2UserInfo;
import com.pen_penned.blog.security.oauth2.user.OAuth2UserInfoFactory;
import com.pen_penned.blog.security.services.UserDetailsImpl;
import com.pen_penned.blog.service.UserService;
import com.pen_penned.blog.util.NameUtils;
import jakarta.transaction.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    public CustomOAuth2UserService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
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

        AuthProvider provider = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new OAuth2AuthenticationProcessingException("Sorry! Login with "
                    + registrationId + " is not supported yet.");
        };

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        // For GitHub: if email is missing, make a separate API call to get it
        if (provider == AuthProvider.GITHUB && !StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            // Get the access token from the request
            String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();

            // Use RestTemplate to fetch emails
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            try {
                ResponseEntity<List<Map<String, Object>>> response =
                        restTemplate.exchange("https://api.github.com/user/emails",
                                HttpMethod.GET, entity,
                                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                                });

                List<Map<String, Object>> emails = response.getBody();

                // Find the primary email
                if (emails != null && !emails.isEmpty()) {
                    for (Map<String, Object> emailEntry : emails) {
                        if (emailEntry.get("primary") != null && (boolean) emailEntry.get("primary")) {
                            String primaryEmail = (String) emailEntry.get("email");
                            // Set the email on the userInfo object
                            oAuth2UserInfo.setEmail(primaryEmail);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw new OAuth2AuthenticationProcessingException("Failed to retrieve email from GitHub");
            }
        }

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
        // Create DTO with user info
        OAuth2UserRequestDTO oAuth2UserRequestDTO = new OAuth2UserRequestDTO();

        // Use the utility to split the name
        String[] nameParts = NameUtils.splitName(oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail());

        oAuth2UserRequestDTO.setFirstName(nameParts[0]);
        oAuth2UserRequestDTO.setLastName(nameParts[1]);
        oAuth2UserRequestDTO.setEmail(oAuth2UserInfo.getEmail());

        oAuth2UserRequestDTO.setEmail(oAuth2UserInfo.getEmail());

        // Use service to create user (no password needed)
        return userService.createOAuth2User(oAuth2UserRequestDTO, provider, oAuth2UserInfo.getId());
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        OAuth2UserRequestDTO oAuth2UserRequestDTO = new OAuth2UserRequestDTO();

        // Use the utility to split the name
        String[] nameParts = NameUtils.splitName(oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail());

        oAuth2UserRequestDTO.setFirstName(nameParts[0]);
        oAuth2UserRequestDTO.setLastName(nameParts[1]);
        oAuth2UserRequestDTO.setEmail(oAuth2UserInfo.getEmail());

        // Use service to update user
        return userService.updateOAuth2User(existingUser, oAuth2UserRequestDTO);
    }

}
