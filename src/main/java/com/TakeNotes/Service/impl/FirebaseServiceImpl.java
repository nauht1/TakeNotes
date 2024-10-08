package com.TakeNotes.Service.impl;

import com.TakeNotes.Document.User;
import com.TakeNotes.Enum.Type;
import com.TakeNotes.Repository.UserRepository;
import com.TakeNotes.Service.IFirebaseService;
import com.TakeNotes.Utils.SecurityUtils;
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

    @Autowired
    private UserRepository userRepository;

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Override
    public String uploadImageToFirebase(MultipartFile imageFile, Type type) throws IOException {
        Storage storage = firebaseStorage;

        User user = SecurityUtils.getCurrentUser(userRepository);
        createUserFolder(user.getId(), user.getEmail());

        // Get file extension
        String originalFilename = imageFile.getOriginalFilename();
        assert originalFilename != null;
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        String imageName;
        String folderName;

        switch (type) {
            case AVATAR:
                imageName = "avatar_image" + "." + fileExtension;
                folderName = MAIN_FOLDER + user.getId() + "-" + user.getEmail() + "/" + INFO_FOLDER + AVATAR_FOLDER;
                break;
            case NOTE:
                imageName = UUID.randomUUID() + "." + fileExtension;
                folderName = MAIN_FOLDER + user.getId() + "-" + user.getEmail() + "/" + NOTE_FOLDER + IMAGE_FOLDER;
                break;
            default:
                imageName = null;
                folderName = null;
                break;
        }

        // Upload image to bucket
        BlobId blobId = BlobId.of(bucketName, folderName + imageName);

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
    public void deleteFileFromFirebase(String url) {
        if (url == null) {
            return;
        }

        // Extract the path part from the URL
        String path = extractPathFromUrl(url);

        if (path == null) {
            return;
        }

        Storage storage = firebaseStorage;
        BlobId blobId = BlobId.of(bucketName, path);
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

    private String extractPathFromUrl(String url) {
        try {
            String[] parts = url.split("/o/");
            if (parts.length > 1) {
                String pathPart = parts[1].split("\\?")[0];
                return java.net.URLDecoder.decode(pathPart, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
