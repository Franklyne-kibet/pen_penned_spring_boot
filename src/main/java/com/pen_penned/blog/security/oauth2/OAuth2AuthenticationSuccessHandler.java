package com.pen_penned.blog.security.oauth2;

import com.pen_penned.blog.security.jwt.JwtUtils;
import com.pen_penned.blog.security.services.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final OAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository;


    public OAuth2AuthenticationSuccessHandler(
            JwtUtils jwtUtils, OAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository) {
        this.jwtUtils = jwtUtils;
        this.oAuth2AuthorizationRequestRepository = oAuth2AuthorizationRequestRepository;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication
            authentication) {
        String redirectUrl = request.getParameter("redirect_uri");

        // Default redirect URL (for frontend URL)
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = "/api/v1/secure"; // TODO: Change this to frontend URL
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateJwtToken(authentication);

        return UriComponentsBuilder.fromUriString(redirectUrl).queryParam("token", token).build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
