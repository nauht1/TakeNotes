package com.TakeNotes.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFirebaseService {
    String uploadImageToFirebase(MultipartFile imageFile) throws IOException;
    void createUserFolder(String userId, String userName);
    void deleteFileFromFirebase(String filePath);
}
