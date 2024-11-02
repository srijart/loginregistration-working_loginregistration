package com.prt.skilltechera.service;

import com.prt.skilltechera.dto.SkillTecheraProfileUpdateRequest;
import com.prt.skilltechera.dto.SkillTecheraRegistrationRequest;
import com.prt.skilltechera.entity.Role;
import com.prt.skilltechera.entity.SkillTecheraUser;
import com.prt.skilltechera.exception.InvalidPasswordException;
import com.prt.skilltechera.exception.PasswordMismatchException;
import com.prt.skilltechera.exception.UserAlreadyRegisteredException;
import com.prt.skilltechera.exception.UserNotFoundException;
import com.prt.skilltechera.repository.RoleRepo;
import com.prt.skilltechera.repository.SkillTecheraUserRepo;
import com.prt.skilltechera.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;

@Service
public class SkillTecheraUserService implements UserDetailsService {

    @Autowired
    private SkillTecheraUserRepo skillTecheraUserRepo;

    @Autowired
    private final JavaMailSender mailSender;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RoleRepo roleRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final Logger logger = LoggerFactory.getLogger(SkillTecheraUserService.class);

    public SkillTecheraUserService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public SkillTecheraUser registerUser(SkillTecheraRegistrationRequest request) {
        validateRegistrationRequest(request);  // Method to handle validation

        SkillTecheraUser skillTecheraUser = new SkillTecheraUser();
        skillTecheraUser.setFullName(request.getFullName());
        skillTecheraUser.setEmail(request.getEmail());
        skillTecheraUser.setPassword(passwordEncoder.encode(request.getPassword()));

        if (skillTecheraUserRepo.findByEmail(skillTecheraUser.getEmail()) != null) {
            logger.info("Username already exists: {}", skillTecheraUser.getEmail());
            throw new UserAlreadyRegisteredException("Username already exists");
        }

        try {
            sendRegistrationEmail(skillTecheraUser.getEmail());  // Separate method for email sending
        } catch (MessagingException e) {
            logger.error("Failed to send registration email to {}", skillTecheraUser.getEmail(), e);
            throw new RuntimeException("Failed to send registration email");
        }

        return skillTecheraUserRepo.save(skillTecheraUser);
    }

    private void validateRegistrationRequest(SkillTecheraRegistrationRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            logger.info("Invalid password: Password is null or less than 8 characters");
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            logger.info("Invalid email: Email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            logger.info("Invalid full name: Full name is null or empty");
            throw new IllegalArgumentException("Full Name cannot be null or empty");
        }
    }

    private void sendRegistrationEmail(String email) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

        messageHelper.setFrom("skilltechera@gmail.com");
        messageHelper.setTo(email);
        messageHelper.setSubject("SkillTechera - Registration");
        messageHelper.setText("You have successfully registered with SkillTechera!");

        ClassPathResource resource = new ClassPathResource("images/SkillTechera_logo.png");
        messageHelper.addAttachment("SkillTechera_logo.png", resource);

        mailSender.send(message);
    }


    public SkillTecheraUser getUserProfileByEmail(String email) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    public SkillTecheraUser updateUserProfile(SkillTecheraProfileUpdateRequest profileUpdateRequest) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(profileUpdateRequest.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + profileUpdateRequest.getEmail());
        }

        if (profileUpdateRequest.getFullName() != null && !profileUpdateRequest.getFullName().trim().isEmpty()) {
            user.setFullName(profileUpdateRequest.getFullName());
        }

        if (profileUpdateRequest.getPassword() != null && profileUpdateRequest.getPassword().length() >= 8) {
            user.setPassword(passwordEncoder.encode(profileUpdateRequest.getPassword()));
        }

        if (profileUpdateRequest.getNewEmail() != null && !profileUpdateRequest.getNewEmail().trim().isEmpty()) {
            if (!profileUpdateRequest.getNewEmail().equals(user.getEmail())) {
                SkillTecheraUser existingUser = skillTecheraUserRepo.findByEmail(profileUpdateRequest.getNewEmail());
                if (existingUser != null) {
                    throw new IllegalArgumentException("Email is already in use");
                }
                sendVerificationEmail(profileUpdateRequest.getNewEmail());
                user.setTemporaryEmail(profileUpdateRequest.getNewEmail());
            }
        }

        return skillTecheraUserRepo.save(user);
    }

    private void sendVerificationEmail(String newEmail) {
        try {
            String token = generateTokenForEmail(newEmail);
            String verificationUrl = "http://localhost:8080/api/v1/verify-email?emailToken=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom("no-reply@skilltechera.com");
            messageHelper.setTo(newEmail);
            messageHelper.setSubject("Verify your new email address");
            messageHelper.setText("Please click the link to verify your new email address: " + verificationUrl);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Failed to send verification email");
        }
    }

    public void completeEmailUpdate(String emailToken) {
        String newEmail = decodeToken(emailToken).getSubject();
        SkillTecheraUser user = skillTecheraUserRepo.findByTemporaryEmail(newEmail);
        if (user == null) {
            throw new UserNotFoundException("User not found for email verification");
        }
        user.setEmail(newEmail);
        user.setTemporaryEmail(null);
        skillTecheraUserRepo.save(user);
    }

    public String loginUserAndGetToken(String email, String password) {
        SkillTecheraUser user = loginUser(email, password);
        return generateTokenForEmail(user.getEmail());
    }

    public SkillTecheraUser loginUser(String email, String password) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Invalid email or password");
        }
        return user;
    }

    public String generateTokenForEmail(String email) {
        return jwtTokenUtil.generateToken(email);
    }

    public Claims decodeToken(String token) {
        return jwtTokenUtil.getClaimsFromToken(token);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), new ArrayList<>()
        );
    }

    @Transactional
    public void assignRoleToUser(String email, String roleName) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        Role role = roleRepo.findByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            roleRepo.save(role);
        }

        if (!user.getRoles().contains(role)) {
            user.addRole(role);
            skillTecheraUserRepo.save(user);
        } else {
            throw new IllegalArgumentException("User already has role: " + roleName);
        }
    }

    @Transactional
    public void removeRoleFromUser(String email, String roleName) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        Role role = roleRepo.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }

        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            skillTecheraUserRepo.save(user);
        } else {
            throw new IllegalArgumentException("User does not have role: " + roleName);
        }
    }

    public void deleteAccount(String email) {
        // Find the user by email
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        // Delete the user from the database
        skillTecheraUserRepo.delete(user);
    }

    public String confirmPassword(String email, String oldPassword, String newPassword, String confirmPassword) {
        SkillTecheraUser user = skillTecheraUserRepo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("New password and confirm password do not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        skillTecheraUserRepo.save(user);

        return "Password updated successfully";
    }
}
