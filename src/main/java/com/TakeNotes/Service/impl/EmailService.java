package com.TakeNotes.Service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private final String frontendDomain = "http://localhost:3000";

    private void sendMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(from, "TakeNotes");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendVerificationMail(String email, String code) {
        String subject = "TakeNotes Verification";

        // use the Frontend domain when click on button in email body
        String verificationUrl = frontendDomain + "/verify?code=" + code;
        String content = "<p>Dear user,</p>"
                + "<p>Please click the link below to verify your registration:</p>"
                + "<p><a href=\"" + verificationUrl + "\">Verify your account</a></p>"
                + "<br>"
                + "<p>TakeNotes Team</p>";

        sendMail(email, subject, content);
    }

}
