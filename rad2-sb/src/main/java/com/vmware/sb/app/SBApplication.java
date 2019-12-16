package com.vmware.sb.app;

import com.vmware.sb.apps.vap.IntegrationConfig;
import com.vmware.sb.aspects.KaminoConfiguration;
import com.vmware.symphony.csp.auth.EnableCspAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@ComponentScan("com.vmware")
@SpringBootApplication
@EnableConfigurationProperties(IntegrationConfig.class)
@EnableCspAuthentication
//@Import(WingtipsWithZipkinSpringBootConfiguration.class)
@KaminoConfiguration(name = "Actor Metrics")
public class SBApplication {
    private static final Logger logger = LoggerFactory.getLogger(SBApplication.class);
    private SBApplicationCmdLineRunner runner;

    @Autowired
    public SBApplication(SBApplicationCmdLineRunner runner) {
        this.runner = runner;
    }

    public static void main(String[] args) {
        try {
            loadPropertiesIntoContext();
            SpringApplication.run(SBApplication.class, args);
        } catch (Exception e) {
            logger.error("Error while starting application - ", e);
            System.exit(1);
        }
    }

    @Profile("!dev")
    private static void loadPropertiesIntoContext() {

        if (StringUtils.isNotBlank(System.getProperty("configPath"))) {
            logger.debug("reading properties from supplied csp.properties");
            Properties cspProperties = readProperties(System.getProperty("configPath") + "/csp/" + "csp" +
                ".properties");
            cspProperties.stringPropertyNames().forEach((key) -> {
                System.setProperty(key, cspProperties.getProperty(key));
            });

            logger.debug("reading properties from supplied application.properties");
            Properties appProperties = readProperties(System.getProperty("configPath") + "/app/" + "application" +
                    ".properties");
            appProperties.stringPropertyNames().forEach((key) -> {
            	System.setProperty(key, appProperties.getProperty(key));
            });
        }
    }

    private static Properties readProperties(String path) {
        Properties p = new Properties();
        try {
            FileReader reader = new FileReader(path);
            p.load(reader);
        } catch (IOException e) {
            logger.warn("Property file - " + path + " not found!");
        }
        return p;
    }
}
