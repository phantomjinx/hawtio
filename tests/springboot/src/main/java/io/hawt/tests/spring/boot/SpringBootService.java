package io.hawt.tests.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URL;

import io.hawt.config.ConfigFacade;
import io.hawt.springboot.HawtioPlugin;
import io.hawt.web.auth.AuthenticationConfiguration;

@SpringBootApplication
public class SpringBootService {
    private static final Logger LOG = LoggerFactory.getLogger(SpringBootService.class);
    private static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";

    public static void main(String[] args) {
        System.setProperty("hawtio.proxyWhitelist", "localhost, 127.0.0.1");
        System.setProperty(AuthenticationConfiguration.HAWTIO_AUTHENTICATION_ENABLED, "false");
        SpringApplication.run(SpringBootService.class, args);
    }

    /**
     * Loading an example plugin.
     */
    @Bean
    public HawtioPlugin samplePlugin() {
        return new HawtioPlugin("sample-plugin",
            "plugins",
            "",
            new String[] {"sample-plugin/sample-plugin.js"});
    }

    /**
     * Configure facade to use authentication.
     *
     * @return config
     * @throws Exception if an error occurs
     */
    @Bean(initMethod = "init")
    public ConfigFacade configFacade() throws Exception {

        final URL loginResource = this.getClass().getClassLoader().getResource("login.conf");
        if (loginResource != null) {
            setSystemPropertyIfNotSet(JAVA_SECURITY_AUTH_LOGIN_CONFIG, loginResource.toExternalForm());
        }
        LOG.info("Using loginResource " + JAVA_SECURITY_AUTH_LOGIN_CONFIG + " : " + System
            .getProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG));

        final URL loginFile = this.getClass().getClassLoader().getResource("realm.properties");
        if (loginFile != null) {
            setSystemPropertyIfNotSet("login.file", loginFile.toExternalForm());
        }
        LOG.info("Using login.file : " + System.getProperty("login.file"));

        setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_ROLES, "admin");
        setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_REALM, "hawtio");
        setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_ROLE_PRINCIPAL_CLASSES, "org.eclipse.jetty.jaas.JAASRole");
        if (!Boolean.getBoolean("debugMode")) {
            System.setProperty(AuthenticationConfiguration.HAWTIO_AUTHENTICATION_ENABLED, "true");
        }
        return new ConfigFacade();
    }

    private void setSystemPropertyIfNotSet(final String key, final String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
