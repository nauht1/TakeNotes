package com.TakeNotes.Controller;

import com.TakeNotes.Model.*;
import com.TakeNotes.Service.impl.AuthenticationService;
import com.TakeNotes.Service.impl.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "register new account")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authenticationService.register(registerDTO));
    }

    @PostMapping("/authenticate")
    @Operation(summary = "normal login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    @PostMapping("/oauth2/login")
    @Operation(summary = "google oauth2 login")
    public ResponseEntity<AuthResponse> loginWithGoogleOauth2(@RequestBody GoogleLoginRequest requestBody) {
        return ResponseEntity.ok(authenticationService.loginOAuthGoogle(requestBody));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "refresh access token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verify")
    @Operation(summary = "use code to verify account")
    public ResponseEntity<String> verify(@RequestParam(value = "code") String code) {
        return ResponseEntity.ok(authenticationService.verifyCode(code));
    }
}
