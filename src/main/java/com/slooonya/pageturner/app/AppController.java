package com.slooonya.pageturner.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

  @GetMapping("/") 
  public String getHomePage() {
      return "home";
  }

  @GetMapping("/error")
  public String showErrorPage() {
      return "error";
  }
}
