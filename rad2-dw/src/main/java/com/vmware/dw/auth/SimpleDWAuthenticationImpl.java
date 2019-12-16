package com.vmware.dw.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Optional;

public class SimpleDWAuthenticationImpl implements Authenticator<BasicCredentials, SimpleDWAuthenticationImpl.User> {
    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if ("secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername()));
        }
        return Optional.empty();
    }

    public static class User implements Principal {
        private String username;

        public User(String username) {
            this.username = username;
        }

        /**
         * Returns the name of this principal.
         *
         * @return the name of this principal.
         */
        @Override
        public String getName() {
            return this.username;
        }

        /**
         * Returns true if the specified subject is implied by this principal. In this implementation check if
         * any of the Principals in the subject has a name that matches this user name. In that case this User
         * implies the Principal
         *
         * <p>The default implementation of this method returns true if
         * {@code subject} is non-null and contains at least one principal that is equal to this principal.
         *
         * <p>Subclasses may override this with a different implementation, if
         * necessary.
         *
         * @param subject the {@code Subject}
         * @return true if {@code subject} is non-null and is implied by this principal, or false otherwise.
         * @since 1.8
         */
        @Override
        public boolean implies(Subject subject) {
            return subject.getPrincipals().stream()
                .filter(x -> x.getName().equalsIgnoreCase(this.getName()))
                .findAny().orElse(null) == null ? false : true;
        }
    }
}
