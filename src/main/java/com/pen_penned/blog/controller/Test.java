package com.pen_penned.blog.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class Test {

    @GetMapping("/secure")
    public String securePage(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            System.out.println("JWT Authenticated: " + token.getName());
        } else if (authentication instanceof OAuth2AuthenticationToken token) {
            System.out.println("OAuth2 Authenticated: " + token.getName());
        }
        return "You are authenticated!";
    }
}
