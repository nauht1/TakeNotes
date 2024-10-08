package com.TakeNotes.Service.impl;

import com.TakeNotes.Document.Token;
import com.TakeNotes.Document.User;
import com.TakeNotes.Enum.Role;
import com.TakeNotes.Enum.TokenType;
import com.TakeNotes.Exception.ResourcesAlreadyExistException;
import com.TakeNotes.Model.*;
import com.TakeNotes.Repository.TokenRepository;
import com.TakeNotes.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.modelmapper.internal.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GoogleIdTokenVerifier verifier;

    public RegisterResponse register(RegisterDTO registerDTO) {
        // random 64bit verification code
        String verificationCode = RandomString.make(64);

        // Find if user has registered yet ?
        Optional<User> existUser = userRepository.findByEmail(registerDTO.getEmail());
        if (existUser.isPresent()) {
            throw new ResourcesAlreadyExistException("Email already exists");
        }

        // Save user
        var user = User.builder()
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .fullName(registerDTO.getFullName())
                .enabled(false)
                .verificationCode(verificationCode)
                .role(Role.USER)
                .created(LocalDateTime.now())
                .build();

        var savedUser = userRepository.save(user);

        // Send email verification
        emailService.sendVerificationMail(user.getEmail(), user.getVerificationCode());

        return RegisterResponse.builder()
                .message("Successfully registered")
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .build();
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail().trim(),
                        authRequest.getPassword().trim()
                )
        );
        // Find if the user is existed?
        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the user is enabled?
        if (!user.isEnabled()) {
            throw new IllegalStateException("User is not verified");
        }

        // Generate JWT and refresh tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Google login
    public AuthResponse loginOAuthGoogle(GoogleLoginRequest requestBody) {
        User user = verifyIDToken(requestBody.getTokenId());
        if (user == null) {
            throw new IllegalArgumentException();
        }
        user = createOauth2User(user);
        System.out.println("USER IDENTIFIED: " + user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Only use for Google oauth2
    @Transactional
    public User createOauth2User(User user) {
        User existingAccount = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingAccount == null) {
            user.setEmail(user.getEmail());
            user.setFullName(user.getFullName());
            user.setRole(Role.USER);
            user.setVerificationCode(null);
            user.setCreated(LocalDateTime.now());
            user.setEnabled(true);
            userRepository.save(user);

            // Send welcome email when it was the first login
            emailService.sendWelcomeMessage(user.getEmail(), user.getFullName());

            return user;
        }
        return existingAccount;
    }

    // Save new token
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .userId(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    // Revoke existing tokens and save the new token
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    // Refresh user token by using available accessToken
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    // Verify code
    public String verifyCode(String code) {
        User user = userRepository.findByVerificationCode(code)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user == null) {
            return "Verification failed";
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeMessage(user.getEmail(), user.getFullName());

        return "Verification successful";
    }

    private User verifyIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String email = payload.getEmail();

            return new User(email, firstName + " " + lastName);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }
}
