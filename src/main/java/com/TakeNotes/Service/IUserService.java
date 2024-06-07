package com.TakeNotes.Service;

import com.TakeNotes.Model.ProfileModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUserService {
    ProfileModel getProfile();
    ProfileModel updateProfile(ProfileModel profile, MultipartFile imageFile) throws IOException;
}
