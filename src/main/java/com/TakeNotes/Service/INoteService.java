package com.TakeNotes.Service;

import com.TakeNotes.Model.CreateNoteDTO;
import com.TakeNotes.Model.NoteModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface INoteService {
    NoteModel createNote(CreateNoteDTO noteDTO, List<MultipartFile> images) throws IOException;
    NoteModel updateNote(String id, NoteModel noteDTO, List<MultipartFile> images) throws Exception;
    String markNote(String id);
    List<NoteModel> getAllNotes();
    List<NoteModel> getAllNotesInTrash();
    List<NoteModel> getALlNotesMarked();
    String deleteImage(String noteId, String imageUrl);
    // Move to trash, or restore from trash
    String move(String noteId);
    // Delete note
    String deleteNote(String noteId);
    String deleteNullNote(String noteId);
    List<NoteModel> findNotesByTitleOrContent(String key);
}
