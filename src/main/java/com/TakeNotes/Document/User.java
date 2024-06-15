package com.TakeNotes.Document;

import com.TakeNotes.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "user")
public class User implements UserDetails {
    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private LocalDate birthday;
    private String avatar_url;
    private String phone;
    private boolean enabled;
    private String verificationCode;
    private Role role;
    private LocalDateTime created;

    public User(String email, String password, String fullName, LocalDate birthday, String avatar_url, String phone, boolean enabled, String verificationCode, Role role, LocalDateTime created) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.birthday = birthday;
        this.avatar_url = avatar_url;
        this.phone = phone;
        this.enabled = enabled;
        this.verificationCode = verificationCode;
        this.role = role;
        this.created = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
