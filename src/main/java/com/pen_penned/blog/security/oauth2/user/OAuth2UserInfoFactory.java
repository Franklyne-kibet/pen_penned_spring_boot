package com.pen_penned.blog.security.oauth2.user;

import com.pen_penned.blog.exception.OAuth2AuthenticationProcessingException;
import com.pen_penned.blog.model.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(AuthProvider authProvider, Map<String, Object> attributes)
            throws OAuth2AuthenticationProcessingException {
        if (AuthProvider.GOOGLE.equals(authProvider)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (AuthProvider.GITHUB.equals(authProvider)) {
            return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with "
                    + authProvider + " is not supported yet.");
        }
    }
}
