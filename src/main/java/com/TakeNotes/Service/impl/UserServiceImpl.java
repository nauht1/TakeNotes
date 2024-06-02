package com.TakeNotes.Service.impl;

import com.TakeNotes.Document.User;
import com.TakeNotes.Model.RegisterDTO;
import com.TakeNotes.Model.UserModel;
import com.TakeNotes.Repository.UserRepository;
import com.TakeNotes.Service.IUserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserModel addUser(RegisterDTO registerDTO) {
        User newUser = modelMapper.map(registerDTO, User.class);
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser, UserModel.class);
    }
}
