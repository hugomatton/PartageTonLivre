package com.udemy.demo.configuration;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
/**
 * Spring security renvoie une page de login par default
 * On est dans le cas d'une single page application
 * On code cette classe pour configurer cet aspect
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint{
    
    @Override
    public void commence(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException authException) throws IOException
    {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
