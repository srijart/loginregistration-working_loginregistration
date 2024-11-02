package com.prt.skilltechera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkillTecheraProfileUpdateRequest {
    private String fullName;
    private String email;
    private String password;  // Optional, only if the user wants to change it
    private String newEmail;
}
