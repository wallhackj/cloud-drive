package com.wallhack.clouddrive.authentication.controller;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.exception.UserAlreadyExistException;
import com.wallhack.clouddrive.authentication.UsersService;
import jakarta.servlet.http.HttpSession;
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
        return "auth/registration";
    }

    @GetMapping("/sign-in")
    public String loginGetPage(Model model){
        model.addAttribute("loginRequest", new UsersPOJO());
        return "auth/login";
    }

//    @GetMapping("/home")
//    public String home(Model model){
//        model.addAttribute("homeRequest", new UsersPOJO());
//        return "home";
//    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute("registerRequest") @Valid UsersPOJO user, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "auth/registration";
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
    public String signIn(@ModelAttribute("loginRequest") @Valid UsersPOJO user, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            UsersPOJO loginUser = usersService.loginUser(user.getUsername(), user.getPassword());

            if (loginUser != null) {
                session.setAttribute("user", user.getUsername());
                return "file_test";
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
