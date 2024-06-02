package com.TakeNotes.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String fullName;
    private LocalDate birthday;
    private String avatar_url;
    private String phone;
    private LocalDateTime created;

    public User(String email, String password, String fullName, LocalDate birthday, String avatar_url, String phone, LocalDateTime created) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.birthday = birthday;
        this.avatar_url = avatar_url;
        this.phone = phone;
        this.created = created;
    }
}
