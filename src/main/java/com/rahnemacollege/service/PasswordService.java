package com.rahnemacollege.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
