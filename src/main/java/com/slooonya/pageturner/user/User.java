package com.slooonya.pageturner.user;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.slooonya.pageturner.role.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="username", nullable=false, length=40, unique=true)
    @Size(min=4, max=20, message="Username must be between 4-20 characters")
    private String username;

    @Column(nullable=false, unique=true)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable=false, length=64)
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 64, message = "Password must be 8-64 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Password must contain at least 1 digit, 1 lowercase, and 1 uppercase letter")
    private String password;

    @Transient
    private String confirmPassword;

    @Column(name ="avatar_file_name")
    private String avatarFileName;

    @Transient
    private MultipartFile avatarFile;

    @Column(name = "bio")
    private String bio;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    private String adminCode;

    @Column(name = "isFrozen", nullable=false)
    private boolean isFrozen = false;

    @Column(name= "timesFlagged", nullable=false)
    private int timesFlagged = 0;

    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public void freezeUser() {
        this.isFrozen = true;
    }
    
    public void unfreezeUser() {
        this.isFrozen = false;
    }

    public void flag(){
        this.timesFlagged++;
    }

    public void unflag(){
        this.timesFlagged--;
    }
}
