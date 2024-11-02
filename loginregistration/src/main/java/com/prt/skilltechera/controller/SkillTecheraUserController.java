package com.prt.skilltechera.controller;

import com.prt.skilltechera.dto.SkillTecheraLoginRequest;
import com.prt.skilltechera.dto.SkillTecheraProfileUpdateRequest;
import com.prt.skilltechera.dto.SkillTecheraRegistrationRequest;
import com.prt.skilltechera.dto.SkillTecheraResponse;
import com.prt.skilltechera.entity.SkillTecheraUser;
import com.prt.skilltechera.exception.UserNotFoundException;
import com.prt.skilltechera.service.PasswordResetService;
import com.prt.skilltechera.service.SkillTecheraUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class SkillTecheraUserController {

    @Autowired
    private SkillTecheraUserService skillTecheraUserService;

    @Autowired
    private PasswordResetService passwordResetService;

    // Register user with validation and consistent response handling
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SkillTecheraRegistrationRequest skillTecheraRegistrationRequest, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, "Validation failed", bindingResult.getFieldErrors()));
        }
        try {
            SkillTecheraUser registeredUser = skillTecheraUserService.registerUser(skillTecheraRegistrationRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Login user and return token
    @PostMapping("/loginWithUserName")
    public ResponseEntity<?> loginWithUserName(@Valid @RequestBody SkillTecheraLoginRequest skillTecheraLoginRequest){
        try {
            String token = skillTecheraUserService.loginUserAndGetToken(skillTecheraLoginRequest.getEmail(), skillTecheraLoginRequest.getPassword());
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Login successful", token));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Get user profile by email
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String email) {
        try {
            SkillTecheraUser userProfile = skillTecheraUserService.getUserProfileByEmail(email);
            return ResponseEntity.ok(userProfile);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Update user profile
    @PutMapping("/profileUpdate")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody SkillTecheraProfileUpdateRequest profileUpdateRequest, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, "Validation failed", bindingResult.getFieldErrors()));
        }
        try {
            SkillTecheraUser updatedUser = skillTecheraUserService.updateUserProfile(profileUpdateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Verify email token
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String emailToken) {
        try {
            skillTecheraUserService.completeEmailUpdate(emailToken);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Email verified successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email){
        try{
            passwordResetService.createPasswordResetTokenForSkillTecheraUser(email);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Password reset email sent", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            // Convert the token from String to int
            int resetToken = Integer.parseInt(token);
            passwordResetService.resetPassword(resetToken, newPassword);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Password reset successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }


    // Assign role to user
    @PutMapping("/assignRole")
    public ResponseEntity<?> assignRole(@RequestParam String email, @RequestParam String role) {
        try {
            skillTecheraUserService.assignRoleToUser(email, role);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Role assigned successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Remove role from user
    @PutMapping("/removeRole")
    public ResponseEntity<?> removeRole(@RequestParam String email, @RequestParam String role) {
        try {
            skillTecheraUserService.removeRoleFromUser(email, role);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Role removed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    // Delete account
    @DeleteMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@RequestParam String email) {
        try {
            skillTecheraUserService.deleteAccount(email);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Account deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }

    //Creating new password after login
    @PutMapping("/addPassword")
    public ResponseEntity<?> addPassword(@RequestParam String email, @RequestParam String oldPassword,@RequestParam String newPassword,@RequestParam String confirmPassword) {
        try{
            skillTecheraUserService.confirmPassword(email,oldPassword,newPassword,confirmPassword);
            return ResponseEntity.ok(new SkillTecheraResponse(true, "Password added successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SkillTecheraResponse(false, e.getMessage(), null));
        }
    }
}
