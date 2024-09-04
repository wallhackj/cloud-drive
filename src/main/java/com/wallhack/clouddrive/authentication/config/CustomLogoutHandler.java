package com.wallhack.clouddrive.authentication.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final FindByIndexNameSessionRepository<? extends Session> redisIndexedSessionRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String id = request.getSession(false).getId();
        if (id != null && this.redisIndexedSessionRepository.findById(id) != null) {
            this.redisIndexedSessionRepository.deleteById(id);
        }
    }

}