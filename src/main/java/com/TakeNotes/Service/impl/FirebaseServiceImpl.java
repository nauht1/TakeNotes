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

    private static final String MAIN_FOLDER = "main/";
    private static final String NOTE_FOLDER = "note/";
    private static final String IMAGE_FOLDER = "image/";
    private static final String INFO_FOLDER = "info/";
    private static final String AVATAR_FOLDER = "avatar/";

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
        String folderName = MAIN_FOLDER + userId + "-" + userName + "/";
        Storage storage = firebaseStorage;

        if (!isFolderExists(storage, folderName)) {
            createFolder(storage, null, folderName);

            // user/note
            createFolder(storage, folderName, NOTE_FOLDER);

            // user/note/image
            createFolder(storage, folderName + NOTE_FOLDER, IMAGE_FOLDER);

            // user/info
            createFolder(storage, folderName, INFO_FOLDER);

            // user/info/avatar
            createFolder(storage, folderName + INFO_FOLDER, AVATAR_FOLDER);
        }
    }

    @Override
    public void deleteFileFromFirebase(String filePath) {
        if (filePath == null) {
            return;
        }
        Storage storage = firebaseStorage;
        BlobId blobId = BlobId.of(bucketName, filePath);
        storage.delete(blobId);
    }

    private boolean isFolderExists(Storage storage, String folderName) {
        BlobId blobId = BlobId.of(bucketName, folderName);
        Blob blob = storage.get(blobId);
        return blob != null;
    }

    private void createFolder(Storage storage, String parentFolder, String folderName) {
        String fullPath = parentFolder + folderName + "/";
        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/x-directory").build();
        storage.create(blobInfo);
    }
}
