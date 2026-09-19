package com.slooonya.pageturner.admin;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private AdminService adminService;

    @MockitoBean 
    private UserService userService;


    @Test
    void profile_shouldReturnAdminProfileView() throws Exception {
        User user = new User();
        when(userService.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/admin-profile")
                .requestAttr("_csrf", new DefaultCsrfToken(
                        "X-CSRF-TOKEN",
                        "_csrf",
                        "test-token"
                )))
            .andExpect(status().isOk())
            .andExpect(view().name("admin-profile"))
            .andExpect(model().attribute("user", user));

        verify(userService).getCurrentUser();
    }


   @Test
  void getAdminHome_shouldReturnAdminHomeView() throws Exception {
      mockMvc.perform(
              get("/admin-home")
                  .requestAttr("_csrf", new DefaultCsrfToken(
                      "X-CSRF-TOKEN",
                      "_csrf",
                      "test-token"
                  ))
          )
          .andExpect(status().isOk())
          .andExpect(view().name("admin-home"));
  }


  @Test
  void getViolationLogs_shouldReturnViolationsView() throws Exception {
      mockMvc.perform(
              get("/violations")
                  .requestAttr("_csrf", new DefaultCsrfToken(
                      "X-CSRF-TOKEN",
                      "_csrf",
                      "test-token"
                  ))
          )
          .andExpect(status().isOk())
          .andExpect(view().name("violations"));
  }


  @Test
  void showSortedUsers_shouldReturnUserListWithUsers() throws Exception {
      List<User> users = List.of(
          new User(),
          new User()
      );

      when(adminService.getUsersSortedBy("username", "desc"))
          .thenReturn(users);

      mockMvc.perform(
              get("/user-list")
                  .param("sortField", "username")
                  .param("sortDirection", "desc")
                  .requestAttr("_csrf", new DefaultCsrfToken(
                      "X-CSRF-TOKEN",
                      "_csrf",
                      "test-token"
                  ))
          )
          .andExpect(status().isOk())
          .andExpect(view().name("user-list"))
          .andExpect(model().attribute("users", users))
          .andExpect(model().attribute("sortField", "username"))
          .andExpect(model().attribute("sortDirection", "desc"));

      verify(adminService).getUsersSortedBy("username", "desc");
  }


  @Test
  void showSortedUsers_shouldUseDefaultSorting() throws Exception {
      List<User> users = List.of(new User());

      when(adminService.getUsersSortedBy("id", "asc")).thenReturn(users);

      mockMvc.perform(
              get("/user-list")
                  .requestAttr("_csrf", new DefaultCsrfToken(
                      "X-CSRF-TOKEN",
                      "_csrf",
                      "test-token"
                  ))
          )
          .andExpect(status().isOk())
          .andExpect(view().name("user-list"))
          .andExpect(model().attribute("users", users))
          .andExpect(model().attribute("sortField", "id"))
          .andExpect(model().attribute("sortDirection", "asc"));

      verify(adminService).getUsersSortedBy("id", "asc");
  }


  @Test
  void freezeUsers_shouldCallServiceAndRedirect() throws Exception {
      mockMvc.perform(
              post("/user-list/freeze")
                  .param("selectedUsers", "1", "2")
          )
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/user-list"));

      verify(adminService).freezeUsers(List.of(1L, 2L));
  }


  @Test
  void unfreezeUsers_shouldCallServiceAndRedirect() throws Exception {
      mockMvc.perform(
              post("/user-list/unfreeze")
                  .param("selectedUsers", "1", "2")
          )
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/user-list"));

      verify(adminService).unfreezeUsers(List.of(1L, 2L));
  }


  @Test
  void changeRole_shouldCallServiceAndRedirect() throws Exception {
      mockMvc.perform(
              post("/user-list/change-role")
                  .param("selectedUsers", "1", "2")
                  .param("role", "ADMIN")
          )
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/user-list"));

      verify(adminService).changeUsersRole(List.of(1L, 2L), "ADMIN");
  }
}