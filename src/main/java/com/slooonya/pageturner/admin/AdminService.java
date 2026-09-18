package com.slooonya.pageturner.admin;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.slooonya.pageturner.role.Role;
import com.slooonya.pageturner.role.RoleRepository;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    // private final EmailService emailService;

    public List<User> getUsersSortedBy(String sortField, String sortDirection) {
        Sort sort = Sort.by(
            Sort.Direction.fromString(sortDirection),
            sortField
        );
        
        return userRepository.findAll(sort);
    }

    @Transactional
    public void freezeUsers(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            user.freezeUser();
            // emailService.sendAccountFrozenEmail(user);
        }
    }

    @Transactional
    public void unfreezeUsers(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            user.unfreezeUser();
        }
    }

    @Transactional
    public void changeUsersRole(List<Long> userIds, String roleName) {

        roleRepository.findAll()
            .forEach(role -> System.out.println(role.getName()));;

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() ->
                new IllegalArgumentException("Role not found: " + roleName));

        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            user.getRoles().clear();
            user.getRoles().add(role);
        }
    }
}
