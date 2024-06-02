package com.TakeNotes.Controller;

import com.TakeNotes.Model.RegisterDTO;
import com.TakeNotes.Model.ResponseModel;
import com.TakeNotes.Model.UserModel;
import com.TakeNotes.Service.IUserService;
import com.TakeNotes.Service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private IUserService userService = new UserServiceImpl();

    @PostMapping("/add")
    public ResponseEntity<ResponseModel> addUser(@RequestBody RegisterDTO user) {
        try {
            UserModel userModel = userService.addUser(user);
            return ResponseEntity.ok().body(new ResponseModel(true, "Success!!", userModel));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed", null));
        }
    }
}
