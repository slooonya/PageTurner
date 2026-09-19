package com.slooonya.pageturner.auth;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.slooonya.pageturner.security.SecurityService;
import com.slooonya.pageturner.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityService securityService;

    @GetMapping("/auth")
    public String getAuthPage(Model model, @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout, @RequestParam(required = false) String registered) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("registrationMessage", "Registration successful. You can now sign in.");
        }
        return "auth";
    }

    @PostMapping("/sign-up")
    public String register(@Valid @ModelAttribute("user") User user, 
        BindingResult result,
        @RequestParam(name = "avatar", required = false) MultipartFile avatar, 
        @RequestParam(name = "adminCode", required = false) String adminCode,
        Model model) {

        authService.validateRegistration(user, avatar, result);

        if (result.hasErrors()) {
            model.addAttribute("containerClass", "sign-up-mode");
            return "auth";
        }

        try {
            User savedUser = authService.register(user, avatar, adminCode);
            return "redirect:/verification-pending?email=" + savedUser.getEmail();
        } catch (IOException | AuthService.RegistrationException exception) {
            result.reject("registration", exception.getMessage());
            model.addAttribute("containerClass", "sign-up-mode");
            return "auth";
        }
    }

    @PostMapping("/sign-in")
    public String login(@RequestParam String username, @RequestParam String password,
            HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            securityService.login(username, password, request, response);
            return "redirect:/home";
        } catch (DisabledException e) {
            redirectAttributes.addAttribute("email", username);
            return "redirect:/verification-pending";
        } catch (AccountFrozenException exception) {
            return "redirect:/account-frozen";
        } catch (BadCredentialsException exception) {
            return "redirect:/auth?error";
        }
    }

    @GetMapping("/account-frozen")
    public String accountFrozen() {
        return "account-frozen";
    }
}
