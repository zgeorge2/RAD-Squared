package com.rad2.sb.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Profile("basic")
public class BasicSBSecurityConfig extends SBSecurityConfig {
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .passwordEncoder(passwordEncoder())
            .withUser("basic")
            .password("$2a$10$mOPkr76R9oNZebeUzMU5Ku3iTnNRIgjoEbDP0tqiXQ3INtDxvxys.")
            .roles("USER");
    }
}