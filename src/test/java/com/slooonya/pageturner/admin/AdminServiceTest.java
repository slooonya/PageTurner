package com.slooonya.pageturner.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.slooonya.pageturner.role.Role;
import com.slooonya.pageturner.role.RoleRepository;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.utils.EmailService;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;


    @Test
    void getUsersSortedBy_shouldReturnUsersSortedByRequestedField() {
        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll(
            Sort.by(Sort.Direction.ASC, "id")
        )).thenReturn(users);

        List<User> result = adminService.getUsersSortedBy("id", "asc");

        assertEquals(users, result);

        verify(userRepository).findAll(Sort.by(Sort.Direction.ASC, "id")
        );
    }


    @Test
    void getUsersSortedBy_shouldSupportDescendingSort() {
        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll(
            Sort.by(Sort.Direction.DESC, "username")
        )).thenReturn(users);

        List<User> result = adminService.getUsersSortedBy("username", "desc");

        assertEquals(users, result);

        verify(userRepository).findAll(Sort.by(Sort.Direction.DESC, "username"));
    }


    @Test
    void freezeUsers_shouldFreezeUsersAndSendEmails() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        List<Long> userIds = List.of(1L, 2L);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAllById(userIds)).thenReturn(users);

        adminService.freezeUsers(userIds);

        verify(user1).freezeUser();
        verify(user2).freezeUser();

        verify(emailService).sendAccountFrozenEmail(user1);
        verify(emailService).sendAccountFrozenEmail(user2);

        verify(userRepository).findAllById(userIds);
    }


    @Test
    void unfreezeUsers_shouldUnfreezeUsers() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        List<Long> userIds = List.of(1L, 2L);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAllById(userIds)).thenReturn(users);

        adminService.unfreezeUsers(userIds);

        verify(user1).unfreezeUser();
        verify(user2).unfreezeUser();

        verify(userRepository).findAllById(userIds);
    }


    @Test
    void changeUsersRole_shouldReplaceExistingRoles() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        Role role = mock(Role.class);

        when(roleRepository.findAll()).thenReturn(List.of());

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        when(userRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(user1, user2));

        Set<Role> roles1 = new HashSet<>();
        Set<Role> roles2 = new HashSet<>();

        when(user1.getRoles()).thenReturn(roles1);
        when(user2.getRoles()).thenReturn(roles2);

        adminService.changeUsersRole(List.of(1L, 2L), "USER");

        assertEquals(Set.of(role), roles1);
        assertEquals(Set.of(role), roles2);

        verify(roleRepository).findByName("USER");
        verify(userRepository).findAllById(List.of(1L, 2L));
    }


    @Test
    void changeUsersRole_shouldThrowExceptionWhenRoleDoesNotExist() {
        when(roleRepository.findAll()).thenReturn(List.of());

        when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> adminService.changeUsersRole(
                List.of(1L),
                "INVALID"
            )
        );

        verify(userRepository, never()).findAllById(any());
    }
}