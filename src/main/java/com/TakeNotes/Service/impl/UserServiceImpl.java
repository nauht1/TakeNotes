package com.TakeNotes.Service.impl;

import com.TakeNotes.Document.User;
import com.TakeNotes.Enum.Type;
import com.TakeNotes.Model.ProfileModel;
import com.TakeNotes.Repository.UserRepository;
import com.TakeNotes.Service.IFirebaseService;
import com.TakeNotes.Service.IUserService;
import com.TakeNotes.Utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IFirebaseService firebaseService = new FirebaseServiceImpl();

    @Override
    public ProfileModel getProfile() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        return modelMapper.map(user, ProfileModel.class);
    }

    @Override
    public ProfileModel updateProfile(ProfileModel profile, MultipartFile imageFile) throws IOException {
        User user = SecurityUtils.getCurrentUser(userRepository);

        if (profile.getBirthday() != null) {
            user.setBirthday(profile.getBirthday());
        }

        if (profile.getPhone() != null) {
            user.setPhone(profile.getPhone());
        }

        if (profile.getFullName() != null) {
            user.setFullName(profile.getFullName());
        }

        if (imageFile != null) {
            if (user.getAvatar_url() != null) {
                firebaseService.deleteFileFromFirebase(user.getAvatar_url());
            }
            String imageUrl = firebaseService.uploadImageToFirebase(imageFile, Type.AVATAR);
            user.setAvatar_url(imageUrl);
        }
        userRepository.save(user);
        return modelMapper.map(user, ProfileModel.class);
    }
}
