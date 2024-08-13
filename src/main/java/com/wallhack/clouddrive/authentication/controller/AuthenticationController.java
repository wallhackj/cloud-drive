package com.wallhack.clouddrive.authentication.controller;

import com.wallhack.clouddrive.authentication.dto.AuthDTO;
import com.wallhack.clouddrive.authentication.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @GetMapping("/file")
    public String filePage() {
        return "file";
    }

    @GetMapping("/sign-in")
    public String showLoginPage(Model model) {
        model.addAttribute("AuthDTO", new AuthDTO("", ""));
        return "auth/login";
    }

    @PostMapping("/sign-in")
    public String loginUser(@Valid AuthDTO authDTO,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            Model model) {
        var isAuthenticated = authService.login(authDTO, request, response);
        if (isAuthenticated != null) {
            return isAuthenticated; // Redirect to a user-specific page
        } else {
            model.addAttribute("error", "1"); // Example error code
            return "auth/login";
        }
    }

    @GetMapping("/sign-up")
    public String showRegistrationPage(Model model) {
        model.addAttribute("registerRequest", new AuthDTO("", ""));
        return "auth/registration";
    }

    @PostMapping("/sign-up")
    public String registerUser(@Valid AuthDTO authDTO, Model model) {
        var isRegistered = authService.register(authDTO);
        if (isRegistered != null) {
            return isRegistered; // Redirect to login page after successful registration
        } else {
            model.addAttribute("error", "1"); // Example error code
            return "auth/registration";
        }
    }
}
