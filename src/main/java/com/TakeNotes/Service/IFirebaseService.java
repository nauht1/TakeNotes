package com.TakeNotes.Service;

import com.TakeNotes.Enum.Type;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFirebaseService {
    String uploadImageToFirebase(MultipartFile imageFile, Type type) throws IOException;
    void createUserFolder(String userId, String userName);
    void deleteFileFromFirebase(String url);
}
