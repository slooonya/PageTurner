package com.slooonya.pageturner.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.slooonya.pageturner.auth.AccountFrozenException;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/admin-profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        return "admin-profile";
    }

    @GetMapping("/admin-home")
    public String getAdminHome() {
        return "admin-home";
    }

    @GetMapping("/violations")
    public String getViolationLogs() {
        return "violations";
    }

    @GetMapping("/user-list")
    public String showSortedUsers(
        @RequestParam(required = false) String searchQuery,
        @RequestParam(name = "sortField", defaultValue = "id") String sortField,
        @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
        Model model) {

        List<User> users = adminService.getUsersSortedBy(sortField, sortDirection);

        model.addAttribute("users", users);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        return "user-list";
    }

    @PostMapping("/user-list/freeze")
    public String freezeUsers(
        @RequestParam(name = "selectedUsers") List<Long> selectedUserIds) {

        adminService.freezeUsers(selectedUserIds);

        return "redirect:/user-list";
    }

    @PostMapping("/user-list/unfreeze")
    public String unfreezeUsers(
        @RequestParam(name = "selectedUsers") List<Long> selectedUserIds) {

        adminService.unfreezeUsers(selectedUserIds);

        return "redirect:/user-list";
    }

    @PostMapping("/user-list/change-role")
    public String changeRole(
        @RequestParam(name = "selectedUsers") List<Long> selectedUserIds,
        @RequestParam String role) {

        adminService.changeUsersRole(selectedUserIds, role);

        return "redirect:/user-list";
    }

    @ExceptionHandler(AccountFrozenException.class)
    public String handleFrozenAccount(AccountFrozenException ex) {
        return "redirect:/account-frozen";
    }
}