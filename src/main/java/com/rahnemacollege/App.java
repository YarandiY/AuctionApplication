/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.rahnemacollege;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;


@SpringBootApplication(exclude = {JacksonAutoConfiguration.class}, scanBasePackages = {
        "com.rahnemacollege"})
public class App extends SpringBootServletInitializer{

    public static void main(String[] args) {
        System.err.println(new Date().getTime());
        SpringApplication.run(App.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }


}
