package com.vegaasen.testing.spring3.runner;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Simple overriding class for the SpringJUnit4ClassRunner. We need to do this in order to specify where to locate
 * the properties for each of the various Beans defined.
 *
 * @author <a href="mailto:vegard.aasen@telenor.com">Vegard Aasen</a>
 *
 */
public final class SpringJUnit4ClassRunnerWithSystemProps extends SpringJUnit4ClassRunner {

    private static final String DEFAULT_TEST_PROPERTIES_FILE_NAME = "/cool.local.properties";

    public SpringJUnit4ClassRunnerWithSystemProps(Class<?> clazz) throws InitializationError {
        super(clazz);
        loadSystemProperties();
    }

    private void loadSystemProperties() {
        Properties properties = new Properties();
        try {
            properties.load(SpringJUnit4ClassRunnerWithSystemProps.class.getResourceAsStream(DEFAULT_TEST_PROPERTIES_FILE_NAME));
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (property != null) {
                    System.setProperty(String.valueOf(property.getKey()), String.valueOf(property.getValue()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
