package com.prt.skilltechera.service;

import com.prt.skilltechera.entity.PasswordResetToken;
import com.prt.skilltechera.entity.SkillTecheraUser;
import com.prt.skilltechera.repository.PasswordResetTokenRepo;
import com.prt.skilltechera.repository.SkillTecheraUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private SkillTecheraUserRepo skillTecheraUserRepo;

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private JavaMailSender mailSender;

    private static final int OTP_EXPIRATION_MINUTES = 15;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void createPasswordResetTokenForSkillTecheraUser(String email) {

        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Random random = new Random();
        int token = random.nextInt(9000000)+100000;
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        passwordResetToken.setSkillTecheraUser(user);

        passwordResetTokenRepo.save(passwordResetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Token");
        message.setText("Your password reset token has been created "+ token);
        message.setFrom("Skilltechera@gmail.com");

        mailSender.send(message);
    }

    public void resetPassword(int token , String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepo.findByToken(token);
        if (passwordResetToken == null) {
            throw new RuntimeException("Token not found");
        }

        if(passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token is expired");
        }

        SkillTecheraUser user = passwordResetToken.getSkillTecheraUser();
        //newPassword is being Encoded
        user.setPassword(passwordEncoder.encode(newPassword));
        skillTecheraUserRepo.save(user);
        //Cleaning up the token and it is being deleted after the task
        passwordResetTokenRepo.delete(passwordResetToken);
    }
}
