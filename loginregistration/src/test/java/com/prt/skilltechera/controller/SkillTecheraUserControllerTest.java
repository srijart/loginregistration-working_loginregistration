package com.prt.skilltechera.controller;

import com.prt.skilltechera.dto.SkillTecheraRegistrationRequest;
import com.prt.skilltechera.dto.SkillTecheraResponse;
import com.prt.skilltechera.entity.SkillTecheraUser;
import com.prt.skilltechera.service.SkillTecheraUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkillTecheraUserControllerTest {

    @Mock
    private SkillTecheraUserService skillTecheraUserService;

    @InjectMocks
    private SkillTecheraUserController skillTecheraUserController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUserSuccess() {
        // Arrange
        SkillTecheraRegistrationRequest skillTecheraRegistrationRequest = new SkillTecheraRegistrationRequest();
        skillTecheraRegistrationRequest.setPassword("password123");
        skillTecheraRegistrationRequest.setEmail("john@gmail.com");
        skillTecheraRegistrationRequest.setFullName("Raviteja");

        SkillTecheraUser skillTecheraUser = new SkillTecheraUser();
        skillTecheraUser.setPassword("password123");
        skillTecheraUser.setEmail("john@gmail.com");
        skillTecheraUser.setFullName("Raviteja");

        when(skillTecheraUserService.registerUser(any(SkillTecheraRegistrationRequest.class)))
                .thenReturn(skillTecheraUser);

        // Mock BindingResult for validation
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);  // Assume no validation errors

        // Act
        ResponseEntity<?> response = skillTecheraUserController.registerUser(skillTecheraRegistrationRequest, bindingResult);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());  // Updated to check for 201 CREATED
        SkillTecheraUser returnedUser = (SkillTecheraUser) response.getBody();
        //assertEquals("Raviteja", returnedUser.getFullName());
        assertEquals("john@gmail.com", returnedUser.getEmail());
    }

    @Test
    public void testRegisterUserBadRequest() {
        // Arrange
        SkillTecheraRegistrationRequest skillTecheraRegistrationRequest = new SkillTecheraRegistrationRequest();
        skillTecheraRegistrationRequest.setEmail("john@gmail.com");
        skillTecheraRegistrationRequest.setFullName("Raviteja");

        when(skillTecheraUserService.registerUser(any(SkillTecheraRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Username cannot be null or empty"));

        // Mock BindingResult for validation
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);  // Simulate validation error

        // Act
        ResponseEntity<?> response = skillTecheraUserController.registerUser(skillTecheraRegistrationRequest, bindingResult);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        SkillTecheraResponse responseBody = (SkillTecheraResponse) response.getBody();
        //assertEquals("Validation failed", responseBody.getMessage());  // Adjusted to check for "Validation failed"
    }
}
