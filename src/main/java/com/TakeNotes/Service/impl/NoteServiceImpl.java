package com.TakeNotes.Service.impl;

import com.TakeNotes.Document.Note;
import com.TakeNotes.Document.User;
import com.TakeNotes.Enum.Type;
import com.TakeNotes.Model.CreateNoteDTO;
import com.TakeNotes.Model.NoteModel;
import com.TakeNotes.Repository.NoteRepository;
import com.TakeNotes.Repository.UserRepository;
import com.TakeNotes.Service.IFirebaseService;
import com.TakeNotes.Service.INoteService;
import com.TakeNotes.Utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NoteServiceImpl implements INoteService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private IFirebaseService firebaseService = new FirebaseServiceImpl();

    @Autowired
    private UserRepository userRepository;

    @Override
    public NoteModel createNote(CreateNoteDTO noteDTO, List<MultipartFile> images) throws IOException {
        User user = SecurityUtils.getCurrentUser(userRepository);

        Note note = modelMapper.map(noteDTO, Note.class);
        List<String> imageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = firebaseService.uploadImageToFirebase(image, Type.NOTE);
                imageUrls.add(imageUrl);
            }
        }
        note.setUserId(user.getId());
        note.setImage_urls(imageUrls);
        note.setCreated(LocalDateTime.now());
        note.setActive(true);

        return modelMapper.map(noteRepository.save(note), NoteModel.class);
    }

    @Override
    public NoteModel updateNote(String id, NoteModel noteDTO, List<MultipartFile> images) throws IOException {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Note note;

        if (id == null || id.isEmpty()) {
            note = new Note();
            note.setUserId(user.getId());
        } else {
            note = noteRepository.findById(id).
                    orElseThrow(() -> new RuntimeException("Note not found"));
        }

        if (noteDTO.getTitle() != null) {
            note.setTitle(noteDTO.getTitle());
        }

        if (noteDTO.getContent() != null) {
            note.setContent(noteDTO.getContent());
        }

        List<String> currentImageUrls = note.getImage_urls();
        if (currentImageUrls == null) {
            currentImageUrls = new ArrayList<>();
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = firebaseService.uploadImageToFirebase(image, Type.NOTE);
                currentImageUrls.add(imageUrl);
            }
            note.setImage_urls(currentImageUrls);
        }
        note.setCreated(LocalDateTime.now());
        note.setActive(true);
        return modelMapper.map(noteRepository.save(note), NoteModel.class);
    }

    @Override
    public String markNote(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        if (!note.isActive()) {
            throw new RuntimeException("Note is not active");
        }
        note.setImportant(!note.isImportant());
        noteRepository.save(note);
        return "Success";
    }

    @Override
    public List<NoteModel> getAllNotes() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndActiveIsTrueAndImportantIsFalse(user.getId());
        List<NoteModel> noteModels = new ArrayList<>();
        notes.forEach(note -> noteModels.add(modelMapper.map(note, NoteModel.class)));
        return noteModels;
    }

    @Override
    public List<NoteModel> getAllNotesInTrash() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndActiveIsFalse(user.getId());
        List<NoteModel> noteModels = new ArrayList<>();
        notes.forEach(note -> noteModels.add(modelMapper.map(note, NoteModel.class)));
        return noteModels;
    }

    @Override
    public List<NoteModel> getALlNotesMarked() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndImportantIsTrueAndActiveIsTrue(user.getId());
        List<NoteModel> noteModels = new ArrayList<>();
        notes.forEach(note -> noteModels.add(modelMapper.map(note, NoteModel.class)));
        return noteModels;
    }

    @Override
    public String deleteImage(String noteId, String imageUrl) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        List<String> imageUrls = note.getImage_urls();
        if (imageUrls != null && imageUrls.contains(imageUrl)) {
            imageUrls.remove(imageUrl);
            note.setImage_urls(imageUrls);
            noteRepository.save(note);

            //delete image from firebase
            firebaseService.deleteFileFromFirebase(imageUrl);

            return "Image URL deleted from note";
        } else {
            throw new RuntimeException("Image URL not found in note");
        }
    }

    // Toggle active field for move to trash feature
    @Override
    public String move(String noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setActive(!note.isActive());
        noteRepository.save(note);
        return "Success";
    }

    @Override
    public String deleteNote(String noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (note.isActive()) {
            throw new RuntimeException("Error deleting note");
        }

        List<String> imageUrls = note.getImage_urls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                System.out.println("Deleting image: " + imageUrl);
                firebaseService.deleteFileFromFirebase(imageUrl);
            }
        }
        noteRepository.delete(note);
        return "Success";
    }

    @Override
    public List<NoteModel> findNotesByTitleOrContent(String searchText, String type) {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes;
        if (Objects.equals(type, "HOME")) {
             notes = noteRepository.findByUserIdAndTitleOrContentInHome(user.getId(), searchText);
        }
        else if (Objects.equals(type, "ARCHIVE")) {
            notes = noteRepository.findByUserIdAndTitleOrContentInArchive(user.getId(), searchText);
        }
        else {
            throw new RuntimeException("Note not found");
        }

        return notes.stream().map(note-> modelMapper.map(note, NoteModel.class)).toList();
    }
}
