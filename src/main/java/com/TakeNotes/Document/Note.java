package com.TakeNotes.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "note")
public class Note {
    @Id
    private String id;
    private String userId;
    private String title;
    private String content;
    private List<String> image_urls;
    private boolean important;
    private LocalDateTime created;
    private boolean active;

    public Note(String userId, String title, String content, List<String> image_urls, boolean important, LocalDateTime created) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.image_urls = image_urls;
        this.important = important;
        this.created = created;
    }
}
