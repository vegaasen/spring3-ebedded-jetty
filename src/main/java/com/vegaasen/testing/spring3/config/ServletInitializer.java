package com.vegaasen.testing.spring3.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class ServletInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        final AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
        root.setServletContext(servletContext);
        root.scan("com.vegaasen.testing.spring3");
        root.refresh();

        final ServletRegistration.Dynamic servlet = servletContext.addServlet("spring", new DispatcherServlet(root));
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/*");
    }
}
