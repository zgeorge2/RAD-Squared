package com.rad2.sb.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Profile("dev")
public class DevSBSecurityConfig extends SBSecurityConfig {
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .passwordEncoder(passwordEncoder())
            .withUser("admin")
            .password("$2a$10$Hap2ZICMvjg505J29zAPqeN0y.lyFJACyplJUwWhW8YzZEdPCVKcW")
            .roles("USER");
    }
}