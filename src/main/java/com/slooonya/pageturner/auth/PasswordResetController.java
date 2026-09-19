package com.slooonya.pageturner.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestReset(@RequestParam String email, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String resetUrl = request.getRequestURL()
                .toString()
                .replace(request.getServletPath(), "")
                + "/password-reset";

        passwordResetService.requestReset(email, resetUrl);

        redirectAttributes.addFlashAttribute(
                "message",
                "If an account exists for that email, a password reset link has been sent."
        );

        return "redirect:/forgot-password";
    }

    @GetMapping("/password-reset")
    public String resetPasswordPage(@RequestParam String token,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            passwordResetService.validateToken(token);
            model.addAttribute("token", token);
            return "password-reset";

        } catch (PasswordResetException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/password-reset")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
        @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Passwords do not match."
            );
            return "redirect:/password-reset?token=" + token;
        }

        try {
            passwordResetService.resetPassword(token, password);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Your password has been reset successfully. You can now log in."
            );

            return "redirect:/auth";

        } catch (PasswordResetException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/auth";
        }
    }
}
