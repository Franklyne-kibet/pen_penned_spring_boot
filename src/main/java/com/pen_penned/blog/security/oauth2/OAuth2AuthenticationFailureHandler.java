package com.pen_penned.blog.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository;

    public OAuth2AuthenticationFailureHandler(OAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository) {
        this.oAuth2AuthorizationRequestRepository = oAuth2AuthorizationRequestRepository;
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String redirectUri = request.getParameter("redirect_uri");

        // Default redirect URL in case of failure
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "/login?error=true"; // TODO: Change this to login URL
        } else {
            redirectUri = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", exception.getLocalizedMessage())
                    .build().toUriString();
        }

        oAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
