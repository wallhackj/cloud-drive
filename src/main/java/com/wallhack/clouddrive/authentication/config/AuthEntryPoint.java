package com.wallhack.clouddrive.authentication.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@AllArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {
        this.handlerExceptionResolver.resolveException(request, response, null, authException);
    }
}
