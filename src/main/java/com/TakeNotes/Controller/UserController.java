package com.TakeNotes.Controller;

import com.TakeNotes.Model.ProfileModel;
import com.TakeNotes.Model.ResponseModel;
import com.TakeNotes.Service.IUserService;
import com.TakeNotes.Service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private IUserService userService = new UserServiceImpl();

    @GetMapping("/profile")
    @Operation(summary = "get auth user profile", security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<ResponseModel> getProfile() {
        try {
            ProfileModel profile = userService.getProfile();
            return ResponseEntity.ok(new ResponseModel(true, "Success!", profile));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed", null));
        }
    }

    @PutMapping("/profile/edit")
    @Operation(summary = "edit user profile", security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<ResponseModel> updateProfile(@ModelAttribute ProfileModel profile,
                                                       @RequestParam(value = "imageFile",
                                                               required = false) MultipartFile imageFile) {
        try {
            ProfileModel profileModel = userService.updateProfile(profile, imageFile);
            return ResponseEntity.ok(new ResponseModel(true, "Success!", profileModel));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(false, "Failed", null));
        }
    }
}
