package com.vegaasen.testing.spring3.beans;

import com.vegaasen.testing.spring3.api.OutputService;
import com.vegaasen.testing.spring3.api.impl.OutputServiceImpl;
import com.vegaasen.testing.spring3.api.impl.SomeServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
 * <property name="systemPropertiesMode" value="2"/>
 * <property name="ignoreResourceNotFound" value="false"/>
 * <property name="location" value="classpath:cool-default.properties"/>
 * </bean>
 */
@Configuration("Some Cool Bean that should load Values")
public class SomeCoolBean {

    private static SomeServiceImpl someService;
    private static OutputService outputService;
    private static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer;

    @Bean(name = "propertyPlaceholder")
    public static PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
        if (propertyPlaceholderConfigurer == null) {
            propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
            propertyPlaceholderConfigurer.setSystemPropertiesMode(2);
            propertyPlaceholderConfigurer.setIgnoreResourceNotFound(false);
            propertyPlaceholderConfigurer.setLocation(new ClassPathResource("cool-default.properties"));
        }
        return propertyPlaceholderConfigurer;
    }

    @Value(value = "${max.iterations}")
    private static int iterations;

    @Value(value = "${text.to.output}")
    private static String textToOutput;

    @Bean(name = "someService")
    public static SomeServiceImpl getSomeService() {
        if (someService == null) {
            someService = new SomeServiceImpl();
            someService.setMaxIterations(iterations);
            someService.setTextToOutput(textToOutput);
        }
        return someService;
    }

    @Bean(name = "outputService")
    public static OutputService getOutputService() {
        if (outputService == null) {
            outputService = new OutputServiceImpl();
        }
        return outputService;
    }

}
