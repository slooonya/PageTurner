package com.slooonya.pageturner.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slooonya.pageturner.user.User;

@Controller
public class AuthController {

  @GetMapping("/auth")
  public String getAuthPage(
    Model model, @RequestParam(required = false) String error,
    @RequestParam(required = false) String logout, 
    @RequestParam(required = false) String mode) {
      model.addAttribute("user", new User());

      return "/auth";
  }
}
