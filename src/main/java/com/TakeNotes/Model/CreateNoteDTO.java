package com.TakeNotes.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateNoteDTO {
    private String id;
    private String userId;
    private String title;
    private String content;
    private boolean important;
}
