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

        return modelMapper.map(noteRepository.save(note), NoteModel.class);
    }

    @Override
    public NoteModel updateNote(String id, NoteModel noteDTO, List<MultipartFile> images) throws IOException {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Note note = noteRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Note not found"));

        if (noteDTO.getTitle() != null) {
            note.setTitle(noteDTO.getTitle());
        }

        if (noteDTO.getContent() != null) {
            note.setContent(noteDTO.getContent());
        }

        List<String> currentImageUrls = note.getImage_urls();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = firebaseService.uploadImageToFirebase(image, Type.NOTE);
                currentImageUrls.add(imageUrl);
            }
            note.setImage_urls(currentImageUrls);
        }

        note.setUserId(user.getId());
        note.setCreated(LocalDateTime.now());

        return modelMapper.map(noteRepository.save(note), NoteModel.class);
    }

    @Override
    public String markNote(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        note.setImportant(!note.isImportant());
        noteRepository.save(note);
        return "Success";
    }
}
