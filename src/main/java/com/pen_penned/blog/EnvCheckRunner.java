package com.pen_penned.blog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvCheckRunner implements CommandLineRunner {

    @Value("${frontend.url}")
    private String frontend;

    @Override
    public void run(String... args) {
        System.out.println("✅ FRONTEND_URL: " + frontend);
    }
}
