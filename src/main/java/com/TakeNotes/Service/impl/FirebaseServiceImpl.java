package com.TakeNotes.Service.impl;

import com.TakeNotes.Service.IFirebaseService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseServiceImpl implements IFirebaseService {
    @Autowired
    private Storage firebaseStorage;

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Override
    public String uploadImageToFirebase(MultipartFile imageFile) throws IOException {
        Storage storage = firebaseStorage;

        // Get file extension
        String originalFilename = imageFile.getOriginalFilename();
        assert originalFilename != null;
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        // Generate unique image name with original file extension
        String imageName = UUID.randomUUID() + "." + fileExtension;

        // Upload image to bucket
        BlobId blobId = BlobId.of(bucketName, imageName);

        try (InputStream inputStream = imageFile.getInputStream()) {
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(imageFile.getContentType()).build();
            Blob blob = storage.create(blobInfo, inputStream.readAllBytes());
            return blob.getMediaLink();
        }
    }

    @Override
    public void createUserFolder(String userId, String userName) {

    }

    @Override
    public void deleteFileFromFirebase(String filePath) {

    }
}
