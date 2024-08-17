package com.wallhack.clouddrive.authentication.controller;

import com.wallhack.clouddrive.authentication.dto.AuthDTO;
import com.wallhack.clouddrive.authentication.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @GetMapping("/sign-up")
    public String showRegistrationPage(Model model) {
        model.addAttribute("authDTO", new AuthDTO("", ""));
        return "auth/registration";
    }

    @PostMapping("/sign-up")
    public String registerUser(@Valid @ModelAttribute("authDTO") AuthDTO authDTO, BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Eroare de validare");
            return "auth/registration";
        }

        try {
            var isRegistered = authService.register(authDTO);

            if (isRegistered.equals("login")) {
                return "redirect:/sign-in";
            } else {
                model.addAttribute("error", "1");
            }

        } catch (Exception ex) {
            model.addAttribute("error", "Eroare neașteptată: " + ex.getMessage());
        }

        return "auth/registration";
    }


    @GetMapping("/sign-in")
    public String showLoginPage(Model model) {
        model.addAttribute("authDTO", new AuthDTO("", ""));
        return "auth/login";
    }

    @PostMapping("/sign-in")
    public String loginUser(@Valid @ModelAttribute("authDTO") AuthDTO authDTO, BindingResult result,
                            HttpServletRequest request, HttpServletResponse response, Model model) {
        if (result.hasErrors()) {
            System.out.println("Errors found: " + result.getAllErrors());
            return "auth/login";
        }

        try {
            var isAuthenticated = authService.login(authDTO, request, response);

            if (isAuthenticated.equals("file")) {
                return "file";
            }

        }catch (BadCredentialsException bcEx) {
            model.addAttribute("error", "Bad Credentials"); // Example error code
        }catch (Exception e) {
            model.addAttribute("error", "Unknown Error");
            e.printStackTrace();
        }

        return "auth/login";
    }
}
