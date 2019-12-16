package com.vmware.sb.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//@EnableWebSecurity
public class SBSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * Authorization: Configure Roles to Access
     *
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers("/system/**").hasRole("USER")
            .anyRequest().authenticated()
            .and()
            .headers().frameOptions().disable()
            .and()
            // disable CSRF, until you are ready to add CSRF to requests. else POST doesn't work
            .csrf().disable();
    }

    /**
     * Authentication: Configure user to roles
     *
     * @throws Exception
     */
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .withUser("admin").password("{noop}admin").roles("USER");
    }
}