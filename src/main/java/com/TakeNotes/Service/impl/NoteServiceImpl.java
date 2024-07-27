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
import com.TakeNotes.Utils.EncryptionUtils;
import com.TakeNotes.Utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    private Environment env;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private IFirebaseService firebaseService = new FirebaseServiceImpl();

    @Autowired
    private UserRepository userRepository;

    private static final String secret_key = System.getenv("AES_CONTENT_ENCRYPTION_KEY");
    private final EncryptionUtils encryptionUtils = new EncryptionUtils(secret_key);

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

        // Copy title and content before encryption
        String originalTitle = note.getTitle();
        String originalContent = note.getContent();


        // Encrypt data before save if not empty
        try {
            if (originalTitle != null && !originalTitle.trim().isEmpty()) {
                note.setTitle(encryptionUtils.encrypt(originalTitle));
            }
            if (originalContent != null && !originalContent.trim().isEmpty()) {
                note.setContent(encryptionUtils.encrypt(originalContent));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating note ", e);
        }

        noteRepository.save(note);

        Note originalNote = new Note(
                note.getId(),
                note.getUserId(),
                originalTitle,
                originalContent,
                note.getImage_urls(),
                note.isImportant(),
                note.getCreated(),
                note.isActive()
        );

        return modelMapper.map(originalNote, NoteModel.class);
    }

    @Override
    public NoteModel updateNote(String id, NoteModel noteDTO, List<MultipartFile> images) throws Exception {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Note note;

        if (id == null || id.isEmpty()) {
            note = new Note();
            note.setUserId(user.getId());
        } else {
            note = noteRepository.findById(id).
                    orElseThrow(() -> new RuntimeException("Note not found"));
        }

        String updatedTitle = "", updatedContent = "";

        if (noteDTO.getTitle() != null && !noteDTO.getTitle().trim().isEmpty()) {
            try {
                // track new title before enc
                updatedTitle = noteDTO.getTitle();
                note.setTitle(encryptionUtils.encrypt(updatedTitle));
            } catch (Exception e) {
                throw new RuntimeException("Error encrypting title ", e);
            }
        }

        if (noteDTO.getContent() != null && !noteDTO.getContent().trim().isEmpty()) {
            try {
                // track new content before enc
                updatedContent = noteDTO.getContent();
                note.setContent(encryptionUtils.encrypt(updatedContent));
            } catch (Exception e) {
                throw new RuntimeException("Error encrypting content ", e);
            }
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

        noteRepository.save(note);

        Note originalNote = new Note(
                note.getId(),
                note.getUserId(),
                updatedTitle,
                updatedContent,
                note.getImage_urls(),
                note.isImportant(),
                note.getCreated(),
                note.isActive()
        );

        return modelMapper.map(originalNote, NoteModel.class);
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

    private List<NoteModel> getDecryptedNotes(List<Note> notes) {
        List<NoteModel> noteModels = new ArrayList<>();
        notes.forEach(note -> {
            try {
                if (note.getTitle() != null && !note.getTitle().trim().isEmpty()) {
                    note.setTitle(encryptionUtils.decrypt(note.getTitle()));
                }
                if (note.getContent() != null && !note.getContent().trim().isEmpty()) {
                    note.setContent(encryptionUtils.decrypt(note.getContent()));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting note", e);
            }
            noteModels.add(modelMapper.map(note, NoteModel.class));
        });
        return noteModels;
    }

    @Override
    public List<NoteModel> getAllNotes() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndActiveIsTrueAndImportantIsFalse(user.getId());
        return getDecryptedNotes(notes);
    }

    @Override
    public List<NoteModel> getAllNotesInTrash() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndActiveIsFalse(user.getId());
        return getDecryptedNotes(notes);
    }

    @Override
    public List<NoteModel> getALlNotesMarked() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findAllByUserIdAndImportantIsTrueAndActiveIsTrue(user.getId());
        return getDecryptedNotes(notes);
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

        boolean isActive = note.isActive();
        note.setActive(!isActive);

        // isActive is TRUE meaning notes will be deleted when calling this method.
        if (isActive) {
            note.setDeletedAt(LocalDateTime.now());
        }
        else {
            note.setDeletedAt(null);
        }

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
    public String deleteNullNote(String noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        noteRepository.delete(note);
        return "Success";
    }

    @Override
    public List<NoteModel> findNotesByTitleOrContent(String searchText) {
        User user = SecurityUtils.getCurrentUser(userRepository);

        List<Note> notes = noteRepository.findByUserIdAndTitleOrContent(user.getId(), searchText);
        return getDecryptedNotes(notes);
    }
}
