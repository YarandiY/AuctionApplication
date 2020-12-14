package com.rahnemacollege.configuration;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
public class RequestResponseLoggingFilter implements Filter {

    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
            throw new ServletException("RequestResponseLoggingFilter just supports HTTP requests");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        logger.info("Logging Request: " + req.getMethod(

        ) + " " + req.getRequestURI());

        chain.doFilter(request, response);

        logger.info("Logging Response:" + res.getContentType());
    }

    @Override
    public void destroy() {

    }


    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("*");
        return registrationBean;
    }


}

