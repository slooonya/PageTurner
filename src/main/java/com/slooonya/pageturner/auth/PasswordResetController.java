package com.slooonya.pageturner.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PasswordResetController {

  @GetMapping("/forgot-password")
  public String getForgotPasswordPage(Model model) {
      return "forgot-password";
  }
}
