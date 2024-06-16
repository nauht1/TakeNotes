package com.TakeNotes.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteModel {
    private String id;
    private String userId;
    private String title;
    private String content;
    private List<String> image_urls;
    private boolean important;
    private LocalDateTime created;
    private boolean active;
}
