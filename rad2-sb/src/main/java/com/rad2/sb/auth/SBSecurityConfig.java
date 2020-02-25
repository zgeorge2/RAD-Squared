/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.auth;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SBSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers("/system/**")
            .hasRole("USER")
            .anyRequest()
            .authenticated()
            .and()
            .headers()
            .frameOptions()
            .disable()
            .and()
            // disable CSRF, until you are ready to add CSRF to requests. else POST doesn't work
            .csrf().disable();
    }

    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void main(String[] args) {
        SBSecurityConfig sb = new SBSecurityConfig();
        System.out.println(sb.passwordEncoder().encode("basic"));
    }
}