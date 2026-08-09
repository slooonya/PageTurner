package com.slooonya.pageturner.user;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.slooonya.pageturner.auth.FileStorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        model.addAttribute("user", currentUser(principal));
        return "profile";
    }

    @PatchMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdate request, Principal principal) {
        String username = request.username() == null ? "" : request.username().trim();
        if (!username.matches("[A-Za-z0-9_]{4,20}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username must use 4-20 letters, numbers, or underscores."));
        }

        User user = currentUser(principal);
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "This username is already taken."));
        }

        user.setUsername(username);
        user.setBio(normalizeBio(request.bio()));
        userRepository.save(user);

        return ResponseEntity.ok(
            Map.of("username", user.getUsername(), 
            "bio", user.getBio() == null ? "" : user.getBio())
        );
    }

    @PostMapping("/api/profile/avatar")
    @ResponseBody
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile avatar, Principal principal) {
        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Choose an image to upload."));
        }

        User user = currentUser(principal);
        try {
            fileStorageService.validateFile(avatar);
            String newFileName = fileStorageService.storeAvatar(avatar, user.getUsername());
            String oldFileName = user.getAvatarFileName();
            user.setAvatarFileName(newFileName);
            userRepository.save(user);
            if (oldFileName != null) {
                fileStorageService.deleteAvatar(oldFileName);
            }
            return ResponseEntity.ok(Map.of("avatarUrl", "/avatars/" + newFileName));
        } catch (IOException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    @PostMapping("/api/profile/password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody PasswordChange request, Principal principal) {
        User user = currentUser(principal);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Your current password is incorrect."));
        }
        if (request.newPassword() == null || !request.newPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,64}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Use 8-64 characters with uppercase, lowercase, and a number."));
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "New passwords do not match."));
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avatars/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> avatar(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadAvatar(filename);
            return resource == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        } catch (IOException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists."));
    }

    private String normalizeBio(String bio) {
        if (bio == null || bio.isBlank()) return null;
        return bio.trim().substring(0, Math.min(bio.trim().length(), 280));
    }

    private record ProfileUpdate(String username, String bio) { }
    private record PasswordChange(String currentPassword, String newPassword, String confirmPassword) { }
}
