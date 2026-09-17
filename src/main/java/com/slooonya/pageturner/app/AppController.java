package com.slooonya.pageturner.app;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/home") 
    public String getHomePage(Authentication authentication) {
        if (authentication.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN")
            )) {

            return "redirect:/admin-home";
        }

        if (authentication.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_MODERATOR")
            )) {

            return "redirect:/admin-home";
        }

        return "home";
  }

  @GetMapping("/error")
  public String showErrorPage() {
      return "error";
  }
}
