package com.vmware.dw.auth;

import io.dropwizard.auth.Authorizer;

public class SimpleDWAuthorizationImpl implements Authorizer<SimpleDWAuthenticationImpl.User> {
    @Override
    public boolean authorize(SimpleDWAuthenticationImpl.User user, String role) {
        return user.getName().equals("zgeorge") && role.equalsIgnoreCase("ADMIN");
    }
}