package com.TakeNotes.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final String SERVICE_ACCOUNT_KEY_PATH = System.getenv("SAC_TAKENOTES");

    private static boolean firebaseAppInitialized = false;

    @PostConstruct
    public void initialize() {
        if (!firebaseAppInitialized) {
            try { // Init firebase app, will automatically call by @PostConstruct
                FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options, "TAKESNOTE");
                firebaseAppInitialized = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    public Storage firebaseStorage() throws IOException {
        String projectId = FirebaseApp.getInstance("TAKESNOTE").getOptions().getProjectId();
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH));
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build();
        return storageOptions.getService();
    }
}
