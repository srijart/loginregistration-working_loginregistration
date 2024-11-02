package com.prt.skilltechera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillTecheraResponse {
    private boolean success;
    private String message;
    private Object data;
}
