package com.TakeNotes.Controller;

import com.TakeNotes.Document.Note;
import com.TakeNotes.Model.CreateNoteDTO;
import com.TakeNotes.Model.NoteModel;
import com.TakeNotes.Model.ResponseModel;
import com.TakeNotes.Service.INoteService;
import com.TakeNotes.Service.impl.NoteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/note")
public class NoteController {
    @Autowired
    private INoteService noteService = new NoteServiceImpl();

    @PostMapping("/add")
    @Operation(summary = "create a new note")
    public ResponseEntity<ResponseModel> createNote(@ModelAttribute CreateNoteDTO noteDTO,
                                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            NoteModel noteModel = noteService.createNote(noteDTO, images);
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", noteModel));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", null));
        }
    }

    @PostMapping("/update")
    @Operation(summary = "update current note")
    public ResponseEntity<ResponseModel> updateNote(@RequestParam(value = "id", required = false) String id,
                                                    @ModelAttribute NoteModel noteModel,
                                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            NoteModel updatedNote = noteService.updateNote(id, noteModel, images);
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", updatedNote));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", null));
        }
    }

    @PostMapping("/mark")
    @Operation(summary = "Mark important note to archive section")
    public ResponseEntity<String> markNote(@RequestParam("id") String id) {
        return ResponseEntity.ok(noteService.markNote(id));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all normal notes")
    public ResponseEntity<ResponseModel> getAllNotes() {
        try {
            List<NoteModel> notes = noteService.getAllNotes();
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", notes));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", null));
        }
    }

    @GetMapping("/all/trash")
    @Operation(summary = "get all notes in trash/bin")
    public ResponseEntity<ResponseModel> getAllNotesInTrash() {
        try {
            List<NoteModel> notes = noteService.getAllNotesInTrash();
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", notes));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", null));
        }
    }

    @GetMapping("/all/archive")
    @Operation(summary = "get all archive notes")
    public ResponseEntity<ResponseModel> getAllNotesInArchive() {
        try {
            List<NoteModel> notes = noteService.getALlNotesMarked();
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", notes));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", null));
        }
    }

    @DeleteMapping("/image/delete")
    @Operation(summary = "delete one image from note")
    public ResponseEntity<String> deleteImage(@RequestParam(value = "id") String id,
                                              @RequestParam(value = "imageUrl") String imageUrl) {
        try {
            String res = noteService.deleteImage(id, imageUrl);
            return ResponseEntity.ok(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete image URL from note");
        }
    }

    @PostMapping("/move")
    @Operation(summary = "move normal note to trash")
    public ResponseEntity<String> moveOrRestore(@RequestParam(value = "id") String id) {
        return ResponseEntity.ok(noteService.move(id));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "permanently delete note in trash")
    public ResponseEntity<String> deleteNote(@RequestParam(value = "id") String id) {
        return ResponseEntity.ok(noteService.deleteNote(id));
    }

    @DeleteMapping("/deleteNullNote")
    @Operation(summary = "delete null note")
    public ResponseEntity<String> deleteNullNote(@RequestParam(value = "id") String id) {
        return ResponseEntity.ok(noteService.deleteNullNote(id));
    }

    @GetMapping("/search")
    @Operation(summary = "find notes")
    public ResponseEntity<ResponseModel> searchNote(@RequestParam String searchText) {
        List<NoteModel> noteModels = new ArrayList<>();
        try {
            noteModels = noteService.findNotesByTitleOrContent(searchText);
            return ResponseEntity.ok(new ResponseModel(true, "Success!!", noteModels));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed!", noteModels));
        }
    }
}
