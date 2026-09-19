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
public class VerificationController {

    private final VerificationTokenService verificationTokenService;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            verificationTokenService.verifyEmail(token);
            return "redirect:/auth?verified=true";
        } catch (VerificationException e) {
            redirectAttributes.addAttribute("error", e.getMessage());

            String email = verificationTokenService.getEmailFromToken(token);

            if (email != null)
                redirectAttributes.addAttribute("email", email);

            return "redirect:/verification-error";
        }
    }


    @GetMapping("/verification-error")
    public String verificationError() {
        return "verification-error";
    }


    @GetMapping("/verification-pending")
    public String verificationPending(
            @RequestParam String email,
            @RequestParam(required=false) boolean resent,
            Model model
    ) {
        model.addAttribute("email", email);
        model.addAttribute("resent", resent);

        return "verification-pending";
    }


    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getRequestURL()
                .toString()
                .replace(request.getServletPath(), "")
                + "/verify-email";

            verificationTokenService.resendVerification(email, baseUrl);

            redirectAttributes.addAttribute("email", email);
            redirectAttributes.addAttribute("resent", true);

            return "redirect:/verification-pending";


        } catch (VerificationException e) {
            redirectAttributes.addAttribute("error", e.getMessage());

            return "redirect:/verification-error";
        }
    }
}