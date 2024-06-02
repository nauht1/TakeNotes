package com.TakeNotes.Service;

import com.TakeNotes.Model.RegisterDTO;
import com.TakeNotes.Model.UserModel;

public interface IUserService {
    UserModel addUser(RegisterDTO userModel);
}
