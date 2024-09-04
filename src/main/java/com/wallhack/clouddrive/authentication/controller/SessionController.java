package com.wallhack.clouddrive.authentication.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;

@RestController
public class SessionController {

    private static Object getObject(Object securityContext) throws NoSuchFieldException, IllegalAccessException {
        Field authenticationField = securityContext.getClass().getDeclaredField("authentication");
        authenticationField.setAccessible(true);
        Object authentication = authenticationField.get(securityContext);

        // Reflectively access the principal details
        Field principalField = authentication.getClass().getDeclaredField("principal");
        principalField.setAccessible(true);
        return principalField.get(authentication);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getSessionData(HttpSession session) {
        Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");

        if (securityContext == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SPRING_SECURITY_CONTEXT not found in session.");
        }

        try {
            // Reflectively access the authentication details
            Object principal = getObject(securityContext);

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                return ResponseEntity.ok(username);
            } else {
                return ResponseEntity.status(404).body("Username not found in session.");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return ResponseEntity.status(500).body("Error accessing session data: " + e.getMessage());
        }
    }
}
