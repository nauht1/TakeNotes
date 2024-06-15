package com.TakeNotes.Controller;

import com.TakeNotes.Model.AuthRequest;
import com.TakeNotes.Model.AuthResponse;
import com.TakeNotes.Model.RegisterDTO;
import com.TakeNotes.Model.RegisterResponse;
import com.TakeNotes.Service.impl.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authenticationService.register(registerDTO));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam(value = "code") String code) {
        return ResponseEntity.ok(authenticationService.verifyCode(code));
    }
}
