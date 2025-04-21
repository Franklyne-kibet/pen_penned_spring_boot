package com.pen_penned.blog.security.oauth2.user;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    private String email;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        // First try to get from the custom field if it was set
        if (email != null) {
            return email;
        }
        // Otherwise get from attributes
        return (String) attributes.get("email");
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
