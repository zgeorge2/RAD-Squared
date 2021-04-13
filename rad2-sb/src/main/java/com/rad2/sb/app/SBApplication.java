/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.app;

import com.rad2.sb.aspects.KaminoConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@ComponentScan("com.rad2")
@SpringBootApplication
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
            logger.debug("reading properties from supplied application.properties");
            Properties appProperties = readProperties(System.getProperty("configPath") + "/app/" + "application" +
                    ".properties");
            appProperties.stringPropertyNames().forEach((key) -> System.setProperty(key, appProperties.getProperty(key)));
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
