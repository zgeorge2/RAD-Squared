package com.vmware.dw.bank;

import com.codahale.metrics.health.HealthCheck;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vmware.dw.auth.SimpleDWAuthenticationImpl;
import com.vmware.dw.auth.SimpleDWAuthorizationImpl;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.validator.constraints.NotEmpty;

public class BankApplication extends Application<BankApplication.BankApplicationConfiguration> {
    public static void main(String[] args) {
        try {
            // initialize the DropWizard Server
            new BankApplication().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(BankApplicationConfiguration bankAppCfg, Environment env) throws Exception {
        Config cfg = ConfigFactory.load(bankAppCfg.getAkkaApplicationConfigurationFile());
        BankResource bankRes = new BankResource();
        bankRes.initialize(cfg);

        // register the DW health checks
        env.healthChecks().register("template",
            new BankApplication.BankApplicationHealthCheck(cfg));
        // register all the DW resources
        env.jersey().register(bankRes);
        // register the authenticator
        env.jersey().register(new AuthDynamicFeature(
            new BasicCredentialAuthFilter.Builder<SimpleDWAuthenticationImpl.User>()
                .setAuthenticator(new SimpleDWAuthenticationImpl())
                .setAuthorizer(new SimpleDWAuthorizationImpl())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        env.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        env.jersey().register(new AuthValueFactoryProvider.Binder<>(SimpleDWAuthenticationImpl.User.class));
    }

    @Override
    public String getName() {
        // the name of the DropWizard YAML configuration file for this application
        // located by default in the project root directory
        return "vapaas-core";
    }

    @Override
    public void initialize(Bootstrap<BankApplicationConfiguration> bootstrap) {
    }

    static public class BankApplicationConfiguration extends Configuration {
        @NotEmpty
        private String akkaApplicationConfigurationFile;

        public String getAkkaApplicationConfigurationFile() {
            return akkaApplicationConfigurationFile;
        }
    }

    static public class BankApplicationHealthCheck extends HealthCheck {
        private Config config;

        public BankApplicationHealthCheck(Config config) {
            this.config = config;
        }

        @Override
        protected Result check() throws Exception {
            if (config == null) {
                return Result.unhealthy("Akka DBank Application Params are unspecified!");
            }
            return Result.healthy();
        }
    }
}
