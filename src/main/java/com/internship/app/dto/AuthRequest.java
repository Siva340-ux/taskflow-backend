package com.internship.app.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String name ;
}
