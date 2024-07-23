package com.wallhack.clouddrive.controller;

import com.wallhack.clouddrive.entity.UsersPOJO;
import com.wallhack.clouddrive.service.UserAlreadyExistException;
import com.wallhack.clouddrive.service.UsersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.PasswordRequiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class AuthenticationController {
    private final UsersService usersService;

    @GetMapping("/sign-up")
    public String registrationGetPage(Model model){
        model.addAttribute("registerRequest", new UsersPOJO());
        return "registration";
    }

    @GetMapping("/sign-in")
    public String loginGetPage(Model model){
        model.addAttribute("loginRequest", new UsersPOJO());
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model){
        model.addAttribute("homeRequest", new UsersPOJO());
        return "home";
    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute("registerRequest") @Valid UsersPOJO user, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {

            if (usersService.saveUser(user)){
                return "redirect:/sign-in";
            }
        }catch (UserAlreadyExistException uaeEx){
            return "redirect:/sign-up?error=1";
        }catch (IllegalArgumentException iaEx){
            return "redirect:/sign-up?error=2";
        }

        return "redirect:/sign-in?error=3";
    }

    @PostMapping("/sign-in")
    public String signIn(@ModelAttribute("loginRequest") @Valid UsersPOJO user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            UsersPOJO loginUser = usersService.loginUser(user.getUsername(), user.getPassword());

            if (loginUser != null) {
                return "redirect:/home";
            }else throw new UsernameNotFoundException("Invalid username or password");

        } catch (UsernameNotFoundException unfEx) {

            return "redirect:/sign-in?error=1";
        } catch (IllegalArgumentException iaEx) {

            return "redirect:/sign-in?error=2";
        }catch (PasswordRequiredException prEx) {

            return "redirect:/sign-in?error=3";
        }

    }

}
