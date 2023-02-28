package com.kanwise.user_service.service.password.encoder.implementation;

import com.kanwise.user_service.service.password.encoder.IPasswordEncoderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PasswordEncoderService implements IPasswordEncoderService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean encodedPasswordMatches(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }
}
