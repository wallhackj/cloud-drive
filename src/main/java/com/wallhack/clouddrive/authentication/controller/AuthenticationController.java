package com.wallhack.clouddrive.authentication.controller;

import com.wallhack.clouddrive.authentication.dto.AuthDTO;
import com.wallhack.clouddrive.authentication.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@AllArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @GetMapping("/")
    public String index() {
        return "mainPage";
    }

    @GetMapping("/sign-up")
    public String showRegistrationPage(Model model) {
        model.addAttribute("authDTO", new AuthDTO("", ""));
        return "auth/registration";
    }

    @PostMapping("/sign-up")
    public String registerUser(@Valid @ModelAttribute("authDTO") AuthDTO authDTO, BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Username or password is wrong?");
            return "auth/registration";
        }

        try {
            var isRegistered = authService.register(authDTO);

            if (isRegistered.equals("login")) {
                return "redirect:/sign-in";
            } else {
                log.warn("Something went wrong at registation method!");
                model.addAttribute("error", "Something went wrong !");
            }

        } catch (Exception ex) {
            log.warn("Something went wrong while registering user", ex);
            model.addAttribute("error", "Some error: " + ex.getMessage());
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
                return "index";
            }

        } catch (BadCredentialsException bcEx) {
            log.warn("Bad Credentials");
            model.addAttribute("error", "Bad Credentials");
        } catch (Exception e) {
            log.warn("Error", e);
            model.addAttribute("error", "Unknown Error");
        }

        return "auth/login";
    }
}
