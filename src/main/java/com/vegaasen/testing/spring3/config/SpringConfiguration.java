package com.vegaasen.testing.spring3.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan("com.vegaasen.testing.spring3")
public class SpringConfiguration {
}
