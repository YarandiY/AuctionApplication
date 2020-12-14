package com.rahnemacollege.domain;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Data
public class AuthenticationRequest {

    @Email(message = "Invalid email address.")
    private String email;
    @Size(min = 6, max = 100, message = "Invalid validPassword")
    private String password;
}
