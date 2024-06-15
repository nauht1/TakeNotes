package com.TakeNotes.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileModel {
    private String email;
    private String fullName;
    private LocalDate birthday;
    private String avatar_url;
    private String phone;
    private LocalDateTime created;
}