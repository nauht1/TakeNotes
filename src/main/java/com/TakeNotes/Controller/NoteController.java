package com.TakeNotes.Controller;

import com.TakeNotes.Document.Note;
import com.TakeNotes.Model.CreateNoteDTO;
import com.TakeNotes.Model.NoteModel;
import com.TakeNotes.Model.ResponseModel;
import com.TakeNotes.Service.INoteService;
import com.TakeNotes.Service.impl.NoteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/note")
public class NoteController {
    @Autowired
    private INoteService noteService = new NoteServiceImpl();

    @PostMapping("/add")
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
    public ResponseEntity<String> markNote(@RequestParam("id") String id) {
        return ResponseEntity.ok(noteService.markNote(id));
    }

    @GetMapping("/all")
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
    
    @DeleteMapping("/image/delete")
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
}
