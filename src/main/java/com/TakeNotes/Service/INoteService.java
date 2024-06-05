package com.TakeNotes.Service;

import com.TakeNotes.Model.CreateNoteDTO;
import com.TakeNotes.Model.NoteModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface INoteService {
    NoteModel createNote(CreateNoteDTO noteDTO, List<MultipartFile> images) throws IOException;
    NoteModel updateNote(String id, NoteModel noteDTO, List<MultipartFile> images) throws IOException;
}
