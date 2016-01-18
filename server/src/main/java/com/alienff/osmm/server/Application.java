package com.alienff.osmm.server;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import java.io.File;

/**
 * @author mike
 * @since 17.01.2016 22:21
 */
@Service
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Value("${tomcat.port:8080}")
    private int port;

    @Autowired
    private WebApplicationContext springContext;

    public static void main(String[] args) throws LifecycleException, ServletException {
        final String osmmPropertiesLocation = System.getProperty("osmm.properties.location");
        if (osmmPropertiesLocation != null) {
            final XmlWebApplicationContext context = new XmlWebApplicationContext();
            context.setConfigLocation("classpath:/spring.xml");
            context.refresh();
            context.getBean(Application.class).start();
            System.exit(0);
        } else {
            log.error("Укажите путь к настройкам через -Dosmm.properties.location=file:///full/path/to/osmm.properties");
            System.exit(1);
        }
    }

    private void start() throws LifecycleException {
        final Tomcat tomcat = new Tomcat();
        final Context context = tomcat.addContext("", new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
        Tomcat.addServlet(context, "app", new DispatcherServlet(springContext)).addInitParameter("contextConfigLocation", "classpath:/empty.xml");
        context.addServletMapping("/", "app");
        tomcat.setPort(port);
        tomcat.start();
        tomcat.getServer().await();
    }
}
